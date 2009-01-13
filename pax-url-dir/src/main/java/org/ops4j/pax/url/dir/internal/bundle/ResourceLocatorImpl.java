/*
 * Copyright 2008 Toni Menzel.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.url.dir.internal.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.url.dir.internal.ResourceLocator;

/**
 * Finds resources of the current module under test just by given top-level parent (whatever that is)
 * and name of the class under test using a narrowing approach.
 *
 * @author Toni Menzel (tonit)
 * @since May 30, 2008
 */
public class ResourceLocatorImpl
    implements ResourceLocator
{

    public static final Log logger = LogFactory.getLog( ResourceLocatorImpl.class );

    private File m_topLevelDir;
    private String m_anchor;
    private File m_root;

    public ResourceLocatorImpl( File topLevelDir, String anchor )
        throws IOException
    {
        NullArgumentException.validateNotNull( topLevelDir, "topLevelDir" );
        if( !topLevelDir.exists() || !topLevelDir.canRead() || !topLevelDir.isDirectory() )
        {
            throw new IllegalArgumentException(
                "topLevelDir " + topLevelDir.getAbsolutePath() + " is not a readable folder"
            );
        }
        m_topLevelDir = topLevelDir;
        m_anchor = anchor;
        m_root = findRoot( m_topLevelDir, m_anchor );
    }

    public ResourceLocatorImpl( String targetClassName )
        throws IOException
    {
        this( new File( "." ), targetClassName );
    }

    protected File findRoot( File dir, String targetClassName )
        throws IOException
    {
        if( m_anchor == null )
        {
            return dir;
        }

        for( File f : dir.listFiles() )
        {
            if( !f.isHidden() && f.isDirectory() )
            {
                File r = findRoot( f, targetClassName );
                if( r != null )
                {
                    return r;
                }
            }
            else if( !f.isHidden()) {
                String p = f.getCanonicalPath().replaceAll( "\\\\","/" );
                if (p.endsWith( targetClassName ) ) {
                return new File(
                    f.getCanonicalPath().substring( 0, f.getCanonicalPath().length() - targetClassName.length() )
                );
            }
           }
        }
        // nothing found / must be wrong topevel dir
        return null;
    }

    /**
     * This locates the top level resource folders for the current component
     *
     * @param target to write to
     */
    public void write( JarOutputStream target )
        throws IOException
    {
        NullArgumentException.validateNotNull( target, "target" );

        // determine the real base

        //String anchorfile =(String) m_anchor.get(""); //m_targetClassName.replace( '.', File.separatorChar ) + ".class";

        findClasspathResources( target, m_root, m_root );
        if( m_root != null )
        {
            // convention: use mvn naming conventions to locate the other resource folders
            // File pureClasses = new File( m_root.getParentFile().getCanonicalPath() + "/classes/" );
            // findClasspathResources( target, pureClasses, pureClasses );
        }
        else
        {
            throw new IllegalArgumentException(
                "Anchor " + m_anchor + " (which must be a file located under (" + m_topLevelDir.getAbsolutePath()
                + ") has not been found!"
            );
        }
    }

    private void findClasspathResources( JarOutputStream target, File dir, File base )
        throws IOException
    {
        if( dir != null && dir.canRead() && dir.isDirectory() )
        {
            for( File f : dir.listFiles() )
            {
                if( f.isDirectory() )
                {
                    findClasspathResources( target, f, base );
                }
                else if( !f.isHidden() )
                {
                    writeToTarget( target, f, base );
                    //repo.add(f);
                }
            }
        }
    }

    public File getRoot()
    {
        return m_root;
    }

    private void writeToTarget( JarOutputStream target, File f, File base )
        throws IOException
    {

        String name =
            f.getCanonicalPath().substring( base.getCanonicalPath().length() + 1 ).replace( File.separatorChar, '/' );
        if( name.equals( "META-INF/MANIFEST.MF" ) )
        {
            throw new RuntimeException( "You have specified a " + name
                                        + " in your probe bundle. Please make sure that you don't have it in your project's target folder. Otherwise it would lead to false assumptions and unexpected results."
            );
        }
        FileInputStream fis = new FileInputStream( f );
        try
        {
            write( name, fis, target );

        } finally
        {
            fis.close();
        }
    }

    void write( String name, InputStream fileIn, JarOutputStream target )
        throws IOException
    {
        target.putNextEntry( new JarEntry( name ) );
        StreamUtils.copyStream( fileIn, target, false );
    }

    @Override
    public String toString()
    {
        return "ResourceLocatorImpl{" +
               "m_topLevelDir=" + m_topLevelDir +
               ", m_anchor=" + m_anchor +
               '}';
    }

}