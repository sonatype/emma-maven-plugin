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

// $Id: InstrumentTask.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma.task;

import java.io.File;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * Instrument classes.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 */
public class InstrumentTask extends AbstractTask
{
    private File[] instrumentationPaths = new File[0];

    private boolean merge;

    private File metadataFile;

    private String[] filters = new String[0];

    protected void prepare( Commandline cmd ) throws Exception
    {
        // tell EMMA to instrument classes
        cmd.createArg().setValue( "instr" );

        // set output directory
        cmd.createArg().setValue( "-outdir" );
        cmd.createArg().setValue( getOutputDirectory().getCanonicalPath() );

        // set metadata file
        if ( metadataFile != null )
        {
            cmd.createArg().setValue( "-outfile" );
            cmd.createArg().setValue( metadataFile.getCanonicalPath() );
        }

        cmd.createArg().setValue( "-merge" );
        cmd.createArg().setValue( merge ? "yes" : "no" );
        cmd.createArg().setValue( "-outmode" );
        cmd.createArg().setValue( "fullcopy" );

        // set instrumentation paths
        if ( instrumentationPaths != null )
        {
            for ( int i = 0; i < instrumentationPaths.length; ++i )
            {
                if ( instrumentationPaths[i] == null )
                {
                    continue;
                }
                cmd.createArg().setValue( "-instrpath" );
                cmd.createArg().setValue( instrumentationPaths[i].getCanonicalPath() );
            }
        }

        // set instrumentation filters
        if ( filters != null )
        {
            for ( int i = 0; i < filters.length; ++i )
            {
                if ( filters[i] == null )
                {
                    continue;
                }
                cmd.createArg().setValue( "-filter" );
                cmd.createArg().setValue( filters[i] );
            }
        }
    }

    public File[] getInstrumentationPaths()
    {
        return instrumentationPaths;
    }

    public void setInstrumentationPaths( File[] instrumentationPaths )
    {
        this.instrumentationPaths = instrumentationPaths;
    }

    public boolean isMerge()
    {
        return merge;
    }

    public void setMerge( boolean merge )
    {
        this.merge = merge;
    }

    public File getMetadataFile()
    {
        return metadataFile;
    }

    public void setMetadataFile( File metadataFile )
    {
        this.metadataFile = metadataFile;
    }

    public String[] getFilters()
    {
        return filters;
    }

    public void setFilters( String[] filters )
    {
        this.filters = filters;
    }
}
