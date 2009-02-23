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

// $Id: EmmaInstrumentMojo.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.maven.plugin.emma.task.InstrumentTask;

/**
 * Offline class instrumentor.
 *
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 * @goal instrument
 * @requiresDependencyResolution test
 */
public class EmmaInstrumentMojo
    extends AbstractEmmaMojo
{
    /**
     * Indicates whether the metadata should be merged into the destination <code>metadataFile</code>, if any.
     *
     * @parameter expression="${emma.merge}" default-value="true"
     */
    protected boolean merge;

    /**
     * Instrumentation filters.
     *
     * @parameter
     */
    protected String[] filters;

    /**
     * Specifies the instrumentation paths to use.
     *
     * @parameter
     */
    protected File[] instrumentationPaths;

    /**
     * Location to store class coverage metadata.
     *
     * @parameter expression="${emma.metadataFile}" default-value="${project.build.directory}/coverage.em"
     */
    protected File metadataFile;

    /**
     * Extra parameters for JVM used by EMMA.
     *
     * @parameter expression="${emma.jvmParameters}" default-value="-Xmx256m"
     */
    protected String jvmParameters;

    /**
     * Artifact factory.
     *
     * @component
     */
    private ArtifactFactory factory;

    protected void checkParameters()
        throws MojoExecutionException, MojoFailureException
    {
        super.checkParameters();

        if ( filters == null )
        {
            filters = new String[0];
        }

        if ( instrumentationPaths == null )
        {
            instrumentationPaths = new File[] { new File( project.getBuild().getOutputDirectory() ) };
        }
    }

    protected void doExecute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( getLog().isDebugEnabled() )
        {
            if ( instrumentationPaths != null )
            {
                getLog().debug( "Instrumentation path:" );
                for ( int i = 0; i < instrumentationPaths.length; ++i )
                {
                    getLog().debug( " o " + instrumentationPaths[i].getAbsolutePath() );
                }
            }

            if ( filters != null && filters.length > 0 )
            {
                getLog().debug( "Filters:" );
                for ( int i = 0; i < filters.length; ++i )
                {
                    getLog().debug( " o " + filters[i] );
                }
            }
        }

        final InstrumentTask task = new InstrumentTask();
        task.setPluginClasspath( pluginClasspath );
        task.setLog( getLog() );
        task.setOutputDirectory( outputDirectory );
        task.setFilters( filters );
        task.setPropertyFile( propertyFile );
        task.setVerbose( verbose );
        task.setInstrumentationPaths( instrumentationPaths );
        task.setMerge( merge );
        task.setMetadataFile( metadataFile );
        task.setJvmParameters( jvmParameters );

        getLog().info( "Instrumenting classes with EMMA" );
        task.execute();

        // prepare test execution by adding EMMA dependencies
        addEmmaDependenciesToTestClasspath();
    }

    /**
     * Add EMMA dependency to project test classpath. When tests are executed, EMMA runtime dependency is required.
     *
     * @throws MojoExecutionException if EMMA dependency could not be added
     */
    private void addEmmaDependenciesToTestClasspath()
        throws MojoExecutionException
    {
        // look for EMMA dependency in this plugin classpath
        final Map pluginArtifactMap = ArtifactUtils.artifactMapByVersionlessId( pluginClasspath );
        Artifact emmaArtifact = (Artifact) pluginArtifactMap.get( "emma:emma" );

        if ( emmaArtifact == null )
        {
            // this should not happen
            throw new MojoExecutionException( "Failed to find 'emma' artifact in plugin dependencies" );
        }

        // set EMMA dependency scope to test
        emmaArtifact = artifactScopeToTest( emmaArtifact );

        // add EMMA to project dependencies
        final Set deps = new HashSet();
        if ( project.getDependencyArtifacts() != null )
        {
            deps.addAll( project.getDependencyArtifacts() );
        }
        deps.add( emmaArtifact );
        project.setDependencyArtifacts( deps );
    }

    /**
     * Convert an artifact to a test artifact.
     *
     * @param artifact to convert
     * @return an artifact with a test scope
     */
    private Artifact artifactScopeToTest( Artifact artifact )
    {
        return factory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                       Artifact.SCOPE_TEST, artifact.getType() );
    }
}
