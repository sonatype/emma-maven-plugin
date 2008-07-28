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

// $Id: EmmaUtils.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Utilities.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 */
class EmmaUtils
{
    private EmmaUtils()
    {
    }

    /**
     * Fix EMMA data file locations. EMMA generates data files in wrong locations: this method moves these files to
     * valid locations.
     * 
     * @param project
     *            current Maven project
     * @param dataFiles
     *            to fix
     * @return new data file locations
     */
    public static File[] fixDataFileLocations( MavenProject project, File[] dataFiles )
    {
        final List newDataFiles = new ArrayList();
        for ( int i = 0; i < dataFiles.length; ++i )
        {
            final File src = dataFiles[i];
            if ( !src.exists() )
            {
                // if the file does not exist, we cannot use it
                continue;
            }

            if ( src.getParentFile().equals( project.getBasedir() ) )
            {
                // EMMA generates coverage data files in project root
                // (as it is actually the current directory):
                // move these files to the "target" directory
                final File dst = new File( project.getBuild().getDirectory(), "coverage-" + i + ".ec" );
                try
                {
                    FileUtils.rename( src, dst );
                }
                catch ( IOException e )
                {
                    throw new IllegalStateException( "Failed to move coverage data file: " + src.getAbsolutePath(), e );
                }
                newDataFiles.add( dst );
            }
            else
            {
                newDataFiles.add( src );
            }
        }

        return (File[]) newDataFiles.toArray( new File[newDataFiles.size()] );
    }
}
