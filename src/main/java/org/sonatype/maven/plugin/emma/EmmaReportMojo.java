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

// $Id: EmmaReportMojo.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.vladium.emma.IAppConstants;
import com.vladium.emma.report.ReportProcessor;
import com.vladium.util.XProperties;

/**
 * Instruments, tests, and generates an EMMA report.
 *
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 * @goal emma
 * @execute phase="test" lifecycle="emma"
 * @requiresDependencyResolution test
 */
public class EmmaReportMojo
    extends AbstractMavenReport
{
    private static final String BUNDLE_BASENAME = EmmaReportMojo.class.getPackage().getName() + ".Resources";

    /**
     * Output directory for the report.
     *
     * @parameter default-value="${project.reporting.outputDirectory}/emma"
     * @required
     */
    protected File outputDirectory;

    /**
     * Source locations.
     *
     * @parameter
     */
    protected File[] sourcePaths;

    /**
     * Location to store class coverage metadata.
     *
     * @parameter expression="${emma.metadataFile}" default-value="${project.build.directory}/coverage.em"
     */
    protected File metadataFile;

    /**
     * Class coverage data files.
     *
     * @parameter
     */
    protected File[] dataFiles;

    /**
     * Report encoding.
     *
     * @parameter expression="${emma.report.encoding}" default-value="UTF-8"
     */
    protected String encoding;

    /**
     * Specifies the amount of detail to be included in the generated coverage reports. Valid values are:
     * <code>all</code>, <code>package</code>, <code>source</code>, <code>class</code>, <code>method</code>.
     *
     * @parameter expression="${emma.report.depth}" default-value="method"
     */
    protected String depth;

    /**
     * Specifies which report columns and in which order to use for report generation, as a comma-separated list of
     * column ids.
     *
     * @parameter expression="${emma.report.columns}" default-value="name,class,method,block,line"
     */
    protected String columns;

    /**
     * Specifies report column sorting order, as a comma-separated list of columns ids prefixed with "+" for ascending
     * or "-" for descending directions.
     *
     * @parameter expression="${emma.report.sort}" default-value="+block,+name,+method,+class"
     */
    protected String sort;

    /**
     * Specifies the threshold coverage metrics values for a given set of columns.
     *
     * @parameter expression="${emma.report.metrics}" default-value="method:70,block:70,line:80,class:100"
     */
    protected String metrics;

    /**
     * Sets EMMA verbosity level to <code>verbose</code>.
     *
     * @parameter expression="${emma.verbose}" default-value="false"
     */
    protected boolean verbose;

    /**
     * Extra parameters for JVM used by EMMA.
     *
     * @parameter expression="${emma.jvmParameters}" default-value="-Xmx256m"
     */
    protected String jvmParameters;

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
     * Reports format to generated (comma separated list)
     *
     * @parameter expression="${emma.report.formats}" default-value="html"
     */
    protected String formats;

    /**
     * Site renderer.
     *
     * @component
     */
    protected Renderer siteRenderer;

    private final Map bundles = new WeakHashMap();

    /**
     * Check parameters.
     *
     * @throws MavenReportException if any parameters are wrong
     */
    protected void checkParameters()
        throws MavenReportException
    {
        if ( getLog().isDebugEnabled() )
        {
            verbose = true;
        }

        if ( dataFiles == null )
        {
            dataFiles = new File[] { new File( project.getBasedir(), "coverage.ec" ) };
        }
        if ( sourcePaths == null )
        {
            sourcePaths = new File[] { new File( project.getBuild().getSourceDirectory() ) };
        }
    }

    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        checkParameters();

        if ( !canGenerateReport() )
        {
            return;
        }

        getLog().debug( "Output directory: " + outputDirectory.getAbsolutePath() );

        String[] sources = new String[sourcePaths.length];
        getLog().debug( "Source paths:" );
        for ( int i = 0; i < sourcePaths.length; ++i )
        {
            getLog().debug( " o " + sourcePaths[i] );
            sources[i] = sourcePaths[i].getAbsolutePath();
        }

        // aggregate several EMMA coverage data files
        final File[] newDataFiles = EmmaUtils.fixDataFileLocations( project, dataFiles );
        String[] dataPath = new String[newDataFiles.length + 1];
        for ( int i = 0; i < newDataFiles.length; i++ )
        {
            dataPath[i] = newDataFiles[i].getAbsolutePath();
        }
        dataPath[newDataFiles.length] = metadataFile.getAbsolutePath();

        ReportProcessor reporter = ReportProcessor.create();
        reporter.setAppName( IAppConstants.APP_NAME );
        reporter.setDataPath( dataPath );
        reporter.setSourcePath( sources );
        reporter.setReportTypes( formats.split( "," ) );
        XProperties properties = new XProperties();
        properties.setProperty( "report.html.out.file", new File( outputDirectory, "index.html" ).getAbsolutePath() );
        properties.setProperty( "report.xml.out.file", new File( outputDirectory, "coverage.xml" ).getAbsolutePath() );
        properties.setProperty( "report.txt.out.file", new File( outputDirectory, "coverage.txt" ).getAbsolutePath() );
        reporter.setPropertyOverrides( properties );

        reporter.run();
    }

    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    public String getDescription( Locale locale )
    {
        return getResourceBundle( locale ).getString( "emma.plugin.description" );
    }

    public String getName( Locale locale )
    {
        return getResourceBundle( locale ).getString( "emma.plugin.name" );
    }

    public String getOutputName()
    {
        return "emma/index";
    }

    public boolean isExternalReport()
    {
        return true;
    }

    public boolean canGenerateReport()
    {
        final boolean ready = metadataFile.exists();
        if ( !ready )
        {
            getLog().info(
                           "Not generating EMMA report as the metadata file (" + metadataFile.getName()
                               + ") could not be found" );
        }
        return ready;
    }

    protected ResourceBundle getResourceBundle( Locale locale )
    {
        ResourceBundle bundle = (ResourceBundle) bundles.get( locale );
        if ( bundle == null )
        {
            bundle = ResourceBundle.getBundle( BUNDLE_BASENAME, locale );
            bundles.put( locale, bundle );
        }
        return bundle;
    }
}
