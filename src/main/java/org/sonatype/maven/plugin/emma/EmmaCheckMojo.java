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

// $Id: EmmaCheckMojo.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.vladium.emma.IAppConstants;
import com.vladium.emma.report.ReportProcessor;
import com.vladium.util.XProperties;

/**
 * Check last intrumentation results.
 *
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 * @goal check
 * @execute phase="test" lifecycle="emma"
 * @phase verify
 */
public class EmmaCheckMojo extends AbstractEmmaMojo
{
    private static final Map TAG_2_TYPE = new HashMap();
    static
    {
        TAG_2_TYPE.put( "all", CoverageResult.Type.ALL );
        TAG_2_TYPE.put( "package", CoverageResult.Type.PACKAGE );
        TAG_2_TYPE.put( "class", CoverageResult.Type.CLASS );
        TAG_2_TYPE.put( "method", CoverageResult.Type.METHOD );
    }

    /**
     * Location to XML coverage file.
     *
     * @parameter expression="${emma.coverageFile}"
     *            default-value="${project.reporting.outputDirectory}/emma/coverage.xml"
     * @required
     */
    protected File coverageFile;

    /**
     * Check configuration.
     *
     * @parameter
     */
    protected CheckConfiguration check;

    /**
     * Location to store class coverage metadata.
     *
     * @parameter expression="${emma.metadataFile}" default-value="${project.build.directory}/coverage.em"
     * @required
     */
    protected File metadataFile;

    /**
     * Class coverage data files.
     *
     * @parameter
     */
    protected File[] dataFiles;

    /**
     * Location to store EMMA generated resources.
     *
     * @parameter default-value="${project.reporting.outputDirectory}/emma"
     * @required
     */
    protected File outputDirectory;

    /**
     * Extra parameters for JVM used by EMMA.
     *
     * @parameter expression="${emma.jvmParameters}" default-value="-Xmx256m"
     */
    protected String jvmParameters;

    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        if ( dataFiles == null )
        {
            dataFiles = new File[] { new File( project.getBasedir(), "coverage.ec" ) };
        }

        dataFiles = EmmaUtils.fixDataFileLocations( project, dataFiles );

        if ( coverageFile == null || !coverageFile.exists() )
        {
            if ( dataFiles != null && dataFiles.length > 0 )
            {
                // XML report was not generated: let's generate it now!
                coverageFile = generateReport();
            }
            else
            {
                getLog().info( "Not checking EMMA coverage results, as no results were found" );
                return;
            }
        }
        if ( check == null )
        {
            getLog().info( "Not checking EMMA coverage results, as no configuration was set" );
            return;
        }

        getLog().info( "Checking EMMA coverage results" );

        // read XML coverage results
        final Document doc;
        InputStream input = null;
        try
        {
            input = new FileInputStream( coverageFile );
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( input );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failed to read EMMA coverage results", e );
        }
        finally
        {
            IOUtil.close( input );
        }

        final CoverageResult all;
        final List results = new ArrayList();

        // parse XML coverage results
        final XPath xpath = XPathFactory.newInstance().newXPath();
        try
        {
            final Element allNode = (Element) xpath.evaluate( "/report/data/all", doc, XPathConstants.NODE );
            all = toCoverageResult( xpath, allNode );

            final String[] expressions =
                { "/report/data/all/package", "/report/data/all/package/srcfile/class",
                    "/report/data/all/package/srcfile/class/method" };
            for ( int i = 0; i < expressions.length; ++i )
            {
                final NodeList nodes = (NodeList) xpath.evaluate( expressions[i], doc, XPathConstants.NODESET );
                int size = nodes.getLength();
                for ( int j = 0; j < size; ++j )
                {
                    final CoverageResult r = toCoverageResult( xpath, (Element) nodes.item( j ) );
                    if ( r != null )
                    {
                        results.add( r );
                    }
                }
            }
        }
        catch ( XPathExpressionException e )
        {
            throw new MojoExecutionException( "Failed to parse EMMA coverage results", e );
        }

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "Coverage results:" );
            getLog().debug( " o " + all );

            // sort results for easier read
            Collections.sort( results, new CoverageResultComparator() );

            for ( final Iterator i = results.iterator(); i.hasNext(); )
            {
                final CoverageResult r = (CoverageResult) i.next();
                getLog().debug( " o " + r );
            }
        }

        // check coverage results
        if ( all != null )
        {
            if ( all.getBlockRate() != CoverageResult.UNKNOWN_RATE && all.getBlockRate() < check.getBlockRate() )
            {
                fail();
                return;
            }
            else if ( all.getClassRate() != CoverageResult.UNKNOWN_RATE && all.getClassRate() < check.getClassRate() )
            {
                fail();
                return;
            }
            else if ( all.getMethodRate() != CoverageResult.UNKNOWN_RATE && all.getMethodRate() < check.getMethodRate() )
            {
                fail();
                return;
            }
            else if ( all.getLineRate() != CoverageResult.UNKNOWN_RATE && all.getLineRate() < check.getLineRate() )
            {
                fail();
                return;
            }
        }

        for ( int i = 0; i < check.getRegexes().length; ++i )
        {
            final CheckConfiguration.Regex regex = check.getRegexes()[i];

            for ( final Iterator j = results.iterator(); j.hasNext(); )
            {
                final CoverageResult result = (CoverageResult) j.next();
                if ( !SelectorUtils.match( regex.getPattern(), result.getName() ) )
                {
                    continue;
                }
                if ( result.getBlockRate() != CoverageResult.UNKNOWN_RATE
                                && result.getBlockRate() < regex.getBlockRate() )
                {
                    fail();
                    return;
                }
                else if ( result.getClassRate() != CoverageResult.UNKNOWN_RATE
                                && result.getClassRate() < regex.getClassRate() )
                {
                    fail();
                    return;
                }
                else if ( result.getMethodRate() != CoverageResult.UNKNOWN_RATE
                                && result.getMethodRate() < regex.getMethodRate() )
                {
                    fail();
                    return;
                }
                else if ( result.getLineRate() != CoverageResult.UNKNOWN_RATE
                                && result.getLineRate() < regex.getLineRate() )
                {
                    fail();
                    return;
                }
            }
        }

        getLog().info( "EMMA coverage results are valid" );
    }

    /**
     * Fail Mojo execution if coverage results are not valid.
     */
    private void fail() throws MojoExecutionException
    {
        final String failMsg = "Failed to validate EMMA coverage results: see report for more information";
        if ( check.isHaltOnFailure() )
        {
            throw new MojoExecutionException( failMsg );
        }
        else
        {
            getLog().warn( failMsg );
        }
    }

    /**
     * Convert XML element to {@link CoverageResult} instance.
     */
    private CoverageResult toCoverageResult( XPath xpath, Element elem ) throws XPathExpressionException
    {
        final CoverageResult.Type type = (CoverageResult.Type) TAG_2_TYPE.get( elem.getNodeName() );
        if ( type == null )
        {
            return null;
        }

        final CoverageResult result;
        if ( CoverageResult.Type.ALL.equals( type ) )
        {
            result = new CoverageResult();
        }
        else
        {
            final String name;
            if ( CoverageResult.Type.CLASS.equals( type ) )
            {
                name = fullClassName( elem );
            }
            else if ( CoverageResult.Type.METHOD.equals( type ) )
            {
                name = fullMethodName( elem );
            }
            else
            {
                name = elem.getAttribute( "name" );
            }
            result = new CoverageResult( type, name );
        }

        final NodeList coverageNodes = (NodeList) xpath.evaluate( "coverage", elem, XPathConstants.NODESET );
        final int len = coverageNodes.getLength();
        for ( int i = 0; i < len; ++i )
        {
            final Element coverageElem = (Element) coverageNodes.item( i );
            final String coverageType = coverageElem.getAttribute( "type" );
            if ( StringUtils.isEmpty( coverageType ) )
            {
                continue;
            }
            final String coverageValueStr = coverageElem.getAttribute( "value" );
            if ( StringUtils.isEmpty( coverageValueStr ) )
            {
                continue;
            }

            final int percentIndex = coverageValueStr.indexOf( '%' );
            if ( percentIndex == -1 )
            {
                continue;
            }

            final int coverageValue;
            try
            {
                coverageValue = Integer.parseInt( coverageValueStr.substring( 0, percentIndex ) );
            }
            catch ( NumberFormatException e )
            {
                getLog().debug( "Failed to parse coverage value: " + coverageValueStr, e );
                continue;
            }

            if ( coverageType.startsWith( "class" ) )
            {
                result.setClassRate( coverageValue );
            }
            else if ( coverageType.startsWith( "method" ) )
            {
                result.setMethodRate( coverageValue );
            }
            else if ( coverageType.startsWith( "block" ) )
            {
                result.setBlockRate( coverageValue );
            }
            else if ( coverageType.startsWith( "line" ) )
            {
                result.setLineRate( coverageValue );
            }
        }

        return result;
    }

    /**
     * Get full class name (package + class) for "class" XML element.
     */
    private String fullClassName( Element elem )
    {
        final Element packageElem = (Element) elem.getParentNode().getParentNode();
        final String packageName = packageElem.getAttribute( "name" );
        final String className = elem.getAttribute( "name" );
        return packageName.length() != 0 ? packageName + "." + className : className;
    }

    /**
     * Get full method name (package + class + method) for "method" XML element.
     */
    private String fullMethodName( Element elem )
    {
        final Element classElem = (Element) elem.getParentNode();
        final String name = elem.getAttribute( "name" );
        final int i = name.indexOf( " (" );
        final String methodName = name.substring( 0, i );
        return fullClassName( classElem ) + "." + methodName;
    }

    private File generateReport() throws MojoExecutionException
    {
        String[] dataPath = new String[dataFiles.length + 1];
        for ( int i = 0; i < dataFiles.length; i++ )
        {
            dataPath[i] = dataFiles[i].getAbsolutePath();
        }
        dataPath[dataFiles.length] = metadataFile.getAbsolutePath();

        ReportProcessor reporter = ReportProcessor.create();
        reporter.setAppName( IAppConstants.APP_NAME );
        reporter.setDataPath( dataPath );
        reporter.setSourcePath( new String[0] );
        reporter.setReportTypes( new String[] { "xml" } );
        XProperties properties = new XProperties();
        properties.setProperty( "report.html.out.file", new File( outputDirectory, "index.html" ).getAbsolutePath() );
        properties.setProperty( "report.xml.out.file", new File( outputDirectory, "coverage.xml" ).getAbsolutePath() );
        properties.setProperty( "report.txt.out.file", new File( outputDirectory, "coverage.txt" ).getAbsolutePath() );
        reporter.setPropertyOverrides( properties );

        return new File( outputDirectory, "coverage.xml" );
    }

    /**
     * Sort {@link CoverageResult} instance against their name.
     */
    private static class CoverageResultComparator implements Comparator
    {
        public int compare( Object o1, Object o2 )
        {
            final CoverageResult r1 = (CoverageResult) o1;
            final CoverageResult r2 = (CoverageResult) o2;
            return r1.getName().compareTo( r2.getName() );
        }
    }
}
