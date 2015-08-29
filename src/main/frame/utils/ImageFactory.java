package main.frame.utils;

import java.net.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

/**
 *
 * <p>Title: Image Factory</p>
 * <p>Description: A factory of images</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class ImageFactory extends Component
{

    private HashMap m_cache;
    private static ImageFactory m_imageFactory;

    /**
     * Constructor
     */
    private ImageFactory()
    {
        m_cache = new HashMap();
    }

    /**
     * Get an instance of Image factory
     * @return ImageFactory
     */
    public static ImageFactory getInstance()
    {
        if(m_imageFactory == null)
        {
            m_imageFactory = new ImageFactory();
        }
        return m_imageFactory;
    }

    /**
     * Get an image icon according to name and component
     * @param name String
     * @param cmp Component
     * @return ImageIcon
     */
    public ImageIcon getImage(String name, Component cmp)
    {
        try
        {
            if(name == null || cmp == null || m_cache == null)
            {
                return null;
            }

            Image image = null;
            ImageIcon imageIcon = (ImageIcon)m_cache.get(name);

            // image already cached
            if(imageIcon != null)
            {
                return imageIcon;
            }

            URL url = cmp.getClass().getResource(name);
            if(url == null)
            {
                System.out.println("image url " + name + " is null");
                return null;
            }
            image = Toolkit.getDefaultToolkit().createImage(url);

            MediaTracker tracker = new MediaTracker(cmp);
            tracker.addImage(image, 0);
            tracker.waitForID(0);
            if(tracker.isErrorAny())
            {
                System.out.println("Error loading image " + name);
                return null;
            }

            imageIcon = new ImageIcon(image);
            m_cache.put(name, imageIcon);

            return imageIcon;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
