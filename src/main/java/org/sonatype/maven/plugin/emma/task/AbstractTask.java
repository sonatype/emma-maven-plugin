// EMMA plugin for Maven 2
// Copyright (c) 2007 Alexandre ROMAN and contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

// $Id: AbstractTask.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma.task;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Abstract class for executing EMMA actions.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 */
public abstract class AbstractTask
{
    private List pluginClasspath;

    private Log log;

    private boolean verbose;

    private File propertyFile;

    private File outputDirectory;

    private String jvmParameters;

    public String getJvmParameters()
    {
        return jvmParameters;
    }

    public void setJvmParameters( String jvmParameters )
    {
        this.jvmParameters = jvmParameters;
    }

    public List getPluginClasspath()
    {
        return pluginClasspath;
    }

    public void setPluginClasspath( List pluginClasspath )
    {
        this.pluginClasspath = pluginClasspath;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public File getPropertyFile()
    {
        return propertyFile;
    }

    public void setPropertyFile( File propertyFile )
    {
        this.propertyFile = propertyFile;
    }

    public Log getLog()
    {
        return log;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

    /**
     * Execute EMMA action.
     * 
     * @throws MojoExecutionException
     *             if execution failed
     */
    public final void execute() throws MojoExecutionException
    {
        assert pluginClasspath != null;
        assert outputDirectory != null;

        try
        {
            // invoke EMMA with a legacy Java command
            final Commandline cmd = new Commandline();
            cmd.setExecutable( "java" );

            // build classpath from this plugin dependencies (including EMMA)
            final StringBuffer classpath = new StringBuffer();
            for ( final Iterator i = pluginClasspath.iterator(); i.hasNext(); )
            {
                final Artifact artifact = (Artifact) i.next();
                classpath.append( artifact.getFile().getCanonicalPath() ).append( File.pathSeparator );
            }

            // set Java classpath
            cmd.createArg().setValue( "-classpath" );
            cmd.createArg().setValue( classpath.toString() );

            // set extra JVM parameters
            if ( jvmParameters != null )
            {
                cmd.createArg().setValue( jvmParameters );
            }

            // set Java main class to run
            cmd.createArg().setValue( "emma" );

            // let subclasses add arguments
            prepare( cmd );

            if ( propertyFile != null )
            {
                cmd.createArg().setValue( "-properties" );
                cmd.createArg().setValue( propertyFile.getAbsolutePath() );
            }
            cmd.createArg().setValue( "-exit" );
            if ( verbose )
            {
                cmd.createArg().setValue( "-verbose" );
            }

            if ( getLog().isDebugEnabled() )
            {
                getLog().debug( "Executing EMMA: " + cmd.toString() );
            }

            // execute EMMA
            final StreamConsumer logStreamConsumer = new LogStreamConsumer( getLog() );
            final int ret = CommandLineUtils.executeCommandLine( cmd, logStreamConsumer, logStreamConsumer );
            if ( ret != 0 )
            {
                throw new IllegalStateException( "EMMA exited with error: " + ret );
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to execute EMMA", e );
        }
    }

    /**
     * Prepare EMMA invocation. Implementation should add arguments for EMMA.
     * 
     * @param cmd
     *            command to prepare
     * @throws Exception
     *             if method failed
     */
    protected abstract void prepare( Commandline cmd ) throws Exception;
}
