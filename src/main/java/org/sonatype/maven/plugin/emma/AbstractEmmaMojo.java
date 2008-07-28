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

// $Id: AbstractEmmaMojo.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base for EMMA mojo.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 */
public abstract class AbstractEmmaMojo extends AbstractMojo
{
    /**
     * Sets EMMA verbosity level to <code>verbose</code>.
     * 
     * @parameter expression="${emma.verbose}" default-value="false"
     */
    protected boolean verbose;

    /**
     * Specifies a pathname for an EMMA property override file.
     * 
     * @parameter expression="${emma.properties}"
     */
    protected File propertyFile;

    /**
     * Sets EMMA properties.
     * 
     * @parameter
     */
    protected Properties properties;

    /**
     * Maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Plugin classpath.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List pluginClasspath;

    /**
     * Location to store EMMA generated resources.
     * 
     * @parameter expression="${emma.outputDirectory}" default-value="${project.build.directory}/generated-classes/emma"
     */
    protected File outputDirectory;

    /**
     * Check parameter values.
     * 
     * @throws MojoFailureException
     *             if any parameter is wrong or missing
     */
    protected void checkParameters() throws MojoExecutionException, MojoFailureException
    {
        if ( getLog().isDebugEnabled() )
        {
            verbose = true;
        }

        if ( propertyFile != null && ( !propertyFile.exists() || !propertyFile.canRead() ) )
        {
            getLog().warn( "Failed to read EMMA properties file: ignoring" );
            propertyFile = null;
        }

        if ( properties == null )
        {
            properties = new Properties();
        }
    }

    /**
     * Execute this Mojo.
     * 
     * @throws MojoExecutionException
     *             if execution failed
     * @throws MojoFailureException
     *             if execution failed
     */
    public final void execute() throws MojoExecutionException, MojoFailureException
    {
        final ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        if ( !"java".equals( artifactHandler.getLanguage() ) )
        {
            getLog().info( "Not executing EMMA, as the project is not a Java classpath-capable package" );
            return;
        }

        checkParameters();

        getLog().debug( "Property file: " + ( propertyFile == null ? "<none>" : propertyFile.getName() ) );

        if ( !properties.isEmpty() && getLog().isDebugEnabled() )
        {
            getLog().debug( "Properties:" );

            for ( final Enumeration keys = properties.propertyNames(); keys.hasMoreElements(); )
            {
                final String key = (String) keys.nextElement();
                getLog().debug( " o " + key + "=" + properties.getProperty( key ) );
            }
        }

        doExecute();
    }

    /**
     * Method called by {@link #execute()}.
     * 
     * @throws MojoExecutionException
     *             if execution failed
     * @throws MojoFailureException
     *             if execution failed
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
