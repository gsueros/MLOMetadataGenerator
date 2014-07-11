
import java.util.ArrayList;
import java.util.List;

public class Layer
{

    private int width;
    private int height;
    private String name;
    private List children;
    private int posx;
    private int posy;
    private String glosaryText;

    public Layer()
    {
        children = new ArrayList();
    }

    public List getChildren()
    {
        return children;
    }

    public void setChildren(List children)
    {
        this.children = children;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPosx()
    {
        return posx;
    }

    public void setPosx(int posx)
    {
        this.posx = posx;
    }

    public int getPosy()
    {
        return posy;
    }

    public void setPosy(int posy)
    {
        this.posy = posy;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public String getGlosaryText()
    {
        return glosaryText;
    }

    public void setGlosaryText(String glosaryText)
    {
        this.glosaryText = glosaryText;
    }
}
