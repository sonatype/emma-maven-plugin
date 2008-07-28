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

// $Id: CoverageResult.java 6585 2008-03-28 10:45:54Z bentmann $

package org.sonatype.maven.plugin.emma;

/**
 * Coverage result.
 * 
 * @author <a href="mailto:alexandre.roman@gmail.com">Alexandre ROMAN</a>
 */
class CoverageResult
{
    /**
     * Result type.
     */
    public static class Type
    {
        public static final Type ALL = new Type( 0 );

        public static final Type CLASS = new Type( 2 );

        public static final Type METHOD = new Type( 3 );

        public static final Type PACKAGE = new Type( 1 );

        private final int id;

        private Type( final int id )
        {
            this.id = id;
        }

        public boolean equals( Object obj )
        {
            if ( obj == null || !( obj instanceof Type ) )
            {
                return false;
            }
            if ( obj == this )
            {
                return true;
            }
            return id == ( (Type) obj ).id;
        }

        public int hashCode()
        {
            return id;
        }

        public String toString()
        {
            switch ( id )
            {
                case 0:
                    return "all";
                case 1:
                    return "package";
                case 2:
                    return "class";
                case 3:
                    return "method";
            }
            return "(unknown type)";
        }
    }

    public static final int UNKNOWN_RATE = -1;

    private int blockRate = UNKNOWN_RATE;

    private int classRate = UNKNOWN_RATE;

    private int lineRate = UNKNOWN_RATE;

    private int methodRate = UNKNOWN_RATE;

    private final String name;

    private final Type type;

    public CoverageResult()
    {
        this.type = Type.ALL;
        this.name = "(all classes)";
    }

    public CoverageResult( final Type type, final String name )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "type is required" );
        }
        if ( name == null )
        {
            throw new IllegalArgumentException( "name is required" );
        }
        this.type = type;
        this.name = name;
    }

    public int getBlockRate()
    {
        return blockRate;
    }

    public int getClassRate()
    {
        return classRate;
    }

    public int getLineRate()
    {
        return lineRate;
    }

    public int getMethodRate()
    {
        return methodRate;
    }

    public String getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }

    public void setBlockRate( int blockRate )
    {
        this.blockRate = blockRate;
    }

    public void setClassRate( int classRate )
    {
        this.classRate = classRate;
    }

    public void setLineRate( int lineRate )
    {
        this.lineRate = lineRate;
    }

    public void setMethodRate( int methodRate )
    {
        this.methodRate = methodRate;
    }

    public String toString()
    {
        return type + " " + name + "[classRate=" + classRate + ", methodRate=" + methodRate + ", blockRate="
                        + blockRate + ", lineRate=" + lineRate + "]";
    }
}
