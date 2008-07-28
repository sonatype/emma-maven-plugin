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

// $Id: CheckConfiguration.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

/**
 * Check configuration, used by <code>check</code> goal.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 * @see EmmaCheckMojo
 */
public class CheckConfiguration
{
    private int lineRate;

    private int blockRate;

    private int methodRate;

    private int classRate;

    private boolean haltOnFailure = true;

    private Regex[] regexes = new Regex[0];

    public static class Regex
    {
        private String pattern;

        private int lineRate;

        private int blockRate;

        private int methodRate;

        private int classRate;

        public int getBlockRate()
        {
            return blockRate;
        }

        public void setBlockRate( int blockRate )
        {
            this.blockRate = blockRate;
        }

        public int getClassRate()
        {
            return classRate;
        }

        public void setClassRate( int classRate )
        {
            this.classRate = classRate;
        }

        public int getLineRate()
        {
            return lineRate;
        }

        public void setLineRate( int lineRate )
        {
            this.lineRate = lineRate;
        }

        public int getMethodRate()
        {
            return methodRate;
        }

        public void setMethodRate( int methodRate )
        {
            this.methodRate = methodRate;
        }

        public String getPattern()
        {
            return pattern;
        }

        public void setPattern( String pattern )
        {
            this.pattern = pattern;
        }
    }

    public int getBlockRate()
    {
        return blockRate;
    }

    public void setBlockRate( int blockRate )
    {
        this.blockRate = blockRate;
    }

    public int getClassRate()
    {
        return classRate;
    }

    public void setClassRate( int classRate )
    {
        this.classRate = classRate;
    }

    public boolean isHaltOnFailure()
    {
        return haltOnFailure;
    }

    public void setHaltOnFailure( boolean haltOnFailure )
    {
        this.haltOnFailure = haltOnFailure;
    }

    public int getLineRate()
    {
        return lineRate;
    }

    public void setLineRate( int lineRate )
    {
        this.lineRate = lineRate;
    }

    public int getMethodRate()
    {
        return methodRate;
    }

    public void setMethodRate( int methodRate )
    {
        this.methodRate = methodRate;
    }

    public Regex[] getRegexes()
    {
        return regexes;
    }

    public void setRegexes( Regex[] regexes )
    {
        this.regexes = regexes;
    }
}
