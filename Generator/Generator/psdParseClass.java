 
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
 
public class PListGenerator
{
    private List psdFiles;
    private StringTemplateGroup group;
    private String mainPath;
    private List vinetas;
    private String currectPath;
    private List<Layer> glosaryList;
    private Hashtable hash;

    public PListGenerator(List files, String path, String language)
    {
        mainPath = "";
        vinetas = new ArrayList();
        currectPath = "";
        currectPath = path;
        psdFiles = new ArrayList();
        glosaryList = new ArrayList();
        psdFiles = files;
        try
        {
            group = new StringTemplateGroup(new FileReader("template.stg"), org.antlr.stringtemplate.language.DefaultTemplateLexer.class);
        }
        catch(FileNotFoundException ex) { }
    }

    public static void main(String args[])
    {
        try
        {
            String appPath = "";
            String language = "";
            boolean isCretaePlist = true;
            if(args.length > 0)
            {
                appPath = args[0];
                language = args[1];
                if(args.length > 2)
                {
                    isCretaePlist = false;
                }
            } else
            {
                appPath = System.getProperties().getProperty("user.dir");
                language = "en";
                isCretaePlist = true;
            }
            appPath += "\\inkasfourlands\\init_nature.psd";
            File startingDirectory = new File(appPath);
            List files = getFileListing(startingDirectory);
            String nameGlosary = language.contains("en") ? (new StringBuilder()).append(appPath).append("\\glosario_en.txt").toString() : (new StringBuilder()).append(appPath).append("\\glosario_es.txt").toString();
            PListGenerator psdParse = new PListGenerator(files, appPath, language);
            if(isCretaePlist)
            {
                psdParse.fillHash(nameGlosary);
                psdParse.createPlist();
            } else
            {
                psdParse.createPlistInformation(nameGlosary);
            }
        }
        catch(FileNotFoundException ex)
        {
            System.out.println("Problemas iniciales con argumentos ...");
        }
    }

    public void fillHash(String glosaryPath)
    {
        List ltmp = readGlosary(glosaryPath);
        String tmp = "";
        for(int i = 0; i < ltmp.size(); i++)
        {
            tmp = ((GlosaryData)ltmp.get(i)).getLabel();
            for(int j = 0; j < ltmp.size(); j++)
            {
                if(((GlosaryData)ltmp.get(j)).getText().toLowerCase().contains(tmp.toLowerCase()))
                {
                    int pos = ((GlosaryData)ltmp.get(j)).getText().toLowerCase().indexOf(tmp.toLowerCase());
                    ((GlosaryData)ltmp.get(j)).setText((new StringBuilder()).append(((GlosaryData)ltmp.get(j)).getText().substring(0, pos)).append("&lt;colored&gt;").append(tmp).append("&lt;/colored&gt;").append(((GlosaryData)ltmp.get(j)).getText().substring(pos + tmp.length())).toString());
                }
            }

        }

        hash.put("testprueba", "testprueba");
        for(int i = 0; i < ltmp.size(); i++)
        {
            hash.put(((GlosaryData)ltmp.get(i)).getKey().trim(), (new StringBuilder()).append("&lt;colored&gt;").append(((GlosaryData)ltmp.get(i)).getLabel()).append(": &lt;/colored&gt;").append(((GlosaryData)ltmp.get(i)).getText()).toString());
        }

    }

    public void createPlistInformation(String glosaryPath)
    {
        List ltmp = readGlosary(glosaryPath);
        String tmp = "";
        for(int i = 0; i < ltmp.size(); i++)
        {
            tmp = ((GlosaryData)ltmp.get(i)).getLabel();
            for(int j = 0; j < ltmp.size(); j++)
            {
                if(((GlosaryData)ltmp.get(j)).getText().toLowerCase().contains(tmp.toLowerCase()))
                {
                    int pos = ((GlosaryData)ltmp.get(j)).getText().toLowerCase().indexOf(tmp.toLowerCase());
                    ((GlosaryData)ltmp.get(j)).setText((new StringBuilder()).append(((GlosaryData)ltmp.get(j)).getText().substring(0, pos)).append("&lt;_link&gt;").append(((GlosaryData)ltmp.get(i)).getText()).append("|").append(tmp).append("&lt;/_link&gt;").append(((GlosaryData)ltmp.get(j)).getText().substring(pos + tmp.length())).toString());
                }
            }

        }

        String texto = "";
        for(int i = 0; i < ltmp.size(); i++)
        {
            texto = (new StringBuilder()).append(texto).append("<string>&lt;title&gt;").append(((GlosaryData)ltmp.get(i)).getLabel()).append("&lt;/title&gt;\n").append(((GlosaryData)ltmp.get(i)).getText()).append("</string>\n\n").toString();
        }

        saveFile(texto, (new StringBuilder()).append(currectPath).append("\\information.txt").toString());
    }

    public void createPlist()
    {

        String slash = mainPath.contains("\\") ? "\\" : "/";
        File f = null;
        List pages = new ArrayList();
        String layer = "layer ";
        String chunk = "";
        String tmp = "";
        String data2 = "";
        String name = "";
        String namePsd = "";
        String psdData = "";
        for(int in = 0; in < psdFiles.size(); in++)
        {
            if(((File)psdFiles.get(in)).getName().contains(".psd"))
            {
                String sname = ((File)psdFiles.get(in)).getName();
                sname = sname.substring(0, sname.indexOf(".psd"));
                f = new File((new StringBuilder()).append(currectPath).append("\\").append(sname).toString());
                f.mkdir();
                try
                {
                    String path = ((File)psdFiles.get(in)).getAbsolutePath();
                    int index = path.lastIndexOf("\\");
                    namePsd = path.substring(index + 1).trim();
                    path = path.substring(0, index).trim();
                    mainPath = path;
                    psdData = loadBat(path, namePsd, sname);
                    if(psdData.length() > 0)
                    {
                        name = psdData.substring(psdData.indexOf("-e -w") + 6, psdData.indexOf(".psd"));
                        int rows = Integer.parseInt(psdData.substring(psdData.indexOf("channels") + 9, psdData.indexOf("rows x")).trim());
                        int cols = Integer.parseInt(psdData.substring(psdData.indexOf("rows x") + 6, psdData.indexOf("cols")).trim());
                        int numLayers = Integer.parseInt(psdData.substring(psdData.indexOf("layers") - 3, psdData.indexOf("layers")).trim());
                        for(int j = 0; j < numLayers - 1; j++)
                        {
                            tmp = (new StringBuilder()).append(layer).append(j).toString();
                            chunk = psdData.substring(psdData.indexOf(tmp) + tmp.length() + 1, psdData.indexOf((new StringBuilder()).append(layer).append(j + 1).toString())).trim();
                            getPlistContent(rows, cols, chunk, (new StringBuilder()).append(layer).append(j).toString());
                        }

                        tmp = (new StringBuilder()).append(layer).append(numLayers - 1).toString();
                        chunk = psdData.substring(psdData.indexOf(tmp) + tmp.length() + 1, psdData.indexOf("done")).trim();
                        getPlistContent(rows, cols, chunk, (new StringBuilder()).append(layer).append(numLayers - 1).toString());
                    } else
                    {
                        System.out.println((new StringBuilder()).append("Problemas con PSD: ").append(((File)psdFiles.get(in)).getName()).toString());
                    }
                }
                catch(Exception e)
                {
                    System.out.println((new StringBuilder()).append("Problemas con PSD: ").append(((File)psdFiles.get(in)).getName()).toString());
                }
                List vinetasReorden2 = new ArrayList();
                String sindex = "";
                String sindex2 = "";
                List vinetasTmp = vinetas;
                int size = vinetas.size();
                for(int i = 0; i < size; i++)
                {
                    sindex = (new StringBuilder()).append("/").append(sindex).append(i).append(".png").toString();
                    int j = 0;
                    do
                    {
                        if(j >= vinetas.size())
                        {
                            break;
                        }
                        if(((Layer)vinetas.get(j)).getName().trim().contains(sindex.trim()))
                        {
                            vinetasReorden2.add(vinetas.get(j));
                            vinetas.remove(j);
                            break;
                        }
                        j++;
                    } while(true);
                    int size2 = vinetas.size();
                    for(int k = 0; k <= size2; k++)
                    {
                        sindex2 = (new StringBuilder()).append("/").append(i).append("_").append(k).append(".png").toString();
                        for(j = 0; j < vinetas.size(); j++)
                        {
                            if(((Layer)vinetas.get(j)).getName().contains(sindex2))
                            {
                                vinetasReorden2.add(vinetas.get(j));
                                vinetas.remove(j);
                            }
                        }

                        sindex2 = "";
                    }
                    size2 = vinetas.size() + 1;
                    System.out.println("size + " + size2);
                    for(int k = 0; k <= size2; k++)
                    {
                        sindex2 = (new StringBuilder()).append("/").append(i).append("_").append(k).append("_").toString();
                        //System.out.println("index : " + sindex2);
                        
                        for( j = 0; j < vinetas.size(); j++)
                        {
                            
                            if(((Layer)vinetas.get(j)).getName().contains(sindex2))
                            {
                            	 System.out.println("glosarry + " + glosaryList.size());
                                glosaryList.add((Layer) vinetas.get(j));
                            }
                        }

                        sindex2 = "";
                    }

                    sindex = "";
                    sindex2 = "";
                }

                if (vinetasReorden2.size() != 0)
                	vinetas = vinetasReorden2;
                List vinetasReorden = new ArrayList();
                for(int i = 0; i < vinetas.size(); i++)
                {
                    Layer ltmp = new Layer();
                    if(((Layer)vinetas.get(i)).getName().indexOf("_") >= 0)
                    {
                        continue;
                    }
                    ltmp = (Layer)vinetas.get(i);
                    String padre = ((Layer)vinetas.get(i)).getName();
                    padre = padre.substring(0, padre.indexOf(".png"));
                    padre = (new StringBuilder()).append(padre).append("_").toString();
                    for(int j = 0; j < vinetas.size(); j++)
                    {
                        if(!((Layer)vinetas.get(j)).getName().toLowerCase().contains(padre.toLowerCase()))
                        {
                            continue;
                        }
                        String padre2 = ((Layer)vinetas.get(j)).getName().replace(".png", "_");

                        System.out.println((new StringBuilder()).append("No se encuentra la palabra: ").append(padre2.trim()).append("en el diccionario").toString());
                        System.out.println( glosaryList.size());
                        

                        for(int k = 0; k < glosaryList.size(); k++)
                        {
                            if(!((Layer)glosaryList.get(k)).getName().toLowerCase().contains(padre2.toLowerCase()))
                            {


                                continue;
                            }
                            String tmpName = ((Layer)glosaryList.get(k)).getName().substring(((Layer)glosaryList.get(k)).getName().lastIndexOf("_") + 1, ((Layer)glosaryList.get(k)).getName().indexOf(".png"));
                            String stmp = (String)hash.get(tmpName.trim());
                            
                            if ( tmpName.equals("crop")) {
                                String cpath = padre2 + "crop.png";
                            	
	                            ((Layer)glosaryList.get(k)).setGlosaryText(cpath);
	                            ((Layer)vinetas.get(j)).getChildren().add((Layer)glosaryList.get(k));
                            	
                            }
                            else if ( tmpName.equals("full") ) {
	                            String cpath = padre2 + "full.png";
	                            	
	                            ((Layer)glosaryList.get(k)).setGlosaryText(cpath);
	                            ((Layer)vinetas.get(j)).getChildren().add((Layer)glosaryList.get(k));
                            }
                            else if ( tmpName.equals("map") ) {
	                            String cpath = padre2 + "map.png";
	                            	
	                            ((Layer)glosaryList.get(k)).setGlosaryText(cpath);
	                            ((Layer)vinetas.get(j)).getChildren().add((Layer)glosaryList.get(k));
                            }
                            if(stmp == null)
                            {
                                System.out.println((new StringBuilder()).append("No se encuentra la palabra: ").append(tmpName.trim()).append("en el diccionario").toString());
                            }
                        }

                        ltmp.getChildren().add(vinetas.get(j));
                    }

                    vinetasReorden.add(ltmp);
                }

                StringTemplate plistGlosary = group.getInstanceOf("plistGlosary");
                StringTemplate plistDict = group.getInstanceOf("plistDict");
                StringTemplate plistPage = group.getInstanceOf("plistPage");
                StringTemplate plistFull = group.getInstanceOf("plistFull");
                List listGlosary = new ArrayList();
                List listDict = new ArrayList();
                List listPage = new ArrayList();
                for(int i = 0; i < vinetasReorden.size(); i++)
                {
                    plistDict = group.getInstanceOf("plistDict");
                    if(((Layer)vinetasReorden.get(i)).getChildren().size() > 0)
                    {
                        for(Iterator iter = ((Layer)vinetasReorden.get(i)).getChildren().iterator(); iter.hasNext(); listGlosary.clear())
                        {
                            Layer children = (Layer)iter.next();
                            if(children.getChildren().size() > 0)
                            {
                                for(Iterator iter2 = children.getChildren().iterator(); iter2 .hasNext();)
                                {
                                    Layer glosary = (Layer)iter2.next();
                                    plistGlosary.setAttribute("_posX", glosary.getPosx());
                                    plistGlosary.setAttribute("_posY", glosary.getPosy());
                                    plistGlosary.setAttribute("_width", glosary.getWidth());
                                    plistGlosary.setAttribute("_height", glosary.getHeight());
                                    plistGlosary.setAttribute("_text", glosary.getGlosaryText());
                                    listGlosary.add(plistGlosary.toString());
                                    plistGlosary = group.getInstanceOf("plistGlosary");
                                }

                            }
                            plistDict.setAttribute("_posX", children.getPosx());
                            plistDict.setAttribute("_posY", children.getPosy());
                            plistDict.setAttribute("_width", children.getWidth());
                            plistDict.setAttribute("_height", children.getHeight());
                            plistDict.setAttribute("_pathImage", children.getName());
                            plistDict.setAttribute("_glosary", listGlosary);
                            if(!children.getName().contains("fullpage.png"))
                            {
                                listDict.add(plistDict.toString());
                            }
                            plistDict = group.getInstanceOf("plistDict");
                        }

                        plistPage.setAttribute("_posX", ((Layer)vinetasReorden.get(i)).getPosx());
                        plistPage.setAttribute("_posY", ((Layer)vinetasReorden.get(i)).getPosy());
                        plistPage.setAttribute("_width", ((Layer)vinetasReorden.get(i)).getWidth());
                        plistPage.setAttribute("_height", ((Layer)vinetasReorden.get(i)).getHeight());
                        plistPage.setAttribute("_pathImage", ((Layer)vinetasReorden.get(i)).getName());
                        plistPage.setAttribute("_children", listDict);
                        if(!((Layer)vinetasReorden.get(i)).getName().contains("fullpage.png"))
                        {
                            listPage.add(plistPage.toString());
                        }
                    } else
                    {
                        plistDict.setAttribute("_posX", ((Layer)vinetasReorden.get(i)).getPosx());
                        plistDict.setAttribute("_posY", ((Layer)vinetasReorden.get(i)).getPosy());
                        plistDict.setAttribute("_width", ((Layer)vinetasReorden.get(i)).getWidth());
                        plistDict.setAttribute("_height", ((Layer)vinetasReorden.get(i)).getHeight());
                        plistDict.setAttribute("_pathImage", ((Layer)vinetasReorden.get(i)).getName());
                        if(!((Layer)vinetasReorden.get(i)).getName().contains("fullpage.png"))
                        {
                            listPage.add(plistDict.toString());
                        }
                    }
                    plistDict = group.getInstanceOf("plistDict");
                    plistPage = group.getInstanceOf("plistPage");
                    listDict.clear();
                    listGlosary.clear();
                    vinetas.clear();
                }

                plistFull.setAttribute("_pages", listPage);
                saveFile(plistFull.toString(), (new StringBuilder()).append(mainPath).append(slash).append(sname).append(slash).append("index.xml").toString());
                copyFullImage(mainPath, sname, slash);
            }
            vinetas.clear();
            glosaryList.clear();
        }

    }

    public static List getFileListing(File aStartingDir)
        throws FileNotFoundException
    {
        if(aStartingDir.isDirectory())
        {
            List result = getFileListingNoSort(aStartingDir);
            Collections.sort(result);
            return result;
        } 
        else {
        	
        	     List result = new ArrayList();
        	     result.add(aStartingDir);
                return result;
            
        }
    }

    private static List getFileListingNoSort(File aStartingDir)
        throws FileNotFoundException
    {
        List result = new ArrayList();
        File filesAndDirs[] = aStartingDir.listFiles();
        List filesDirs = Arrays.asList(filesAndDirs);
        Iterator iter = filesDirs.iterator();
        do
        {
            if(!iter.hasNext())
            {
                break;
            }
            File file = (File)iter.next();
            result.add(file);
            if(!file.isFile())
            {
                List deeperList = getFileListingNoSort(file);
                result.addAll(deeperList);
            }
        } while(true);
        return result;
    }

    private void saveFile(String text, String path)
    {
    	System.out.println(" : " + text);
        FileWriter fichero;
        fichero = null;
        try {
			fichero = new FileWriter(path);
		    PrintWriter pw = new PrintWriter(fichero);
	        pw.println(text);
	        
	            if(null != fichero)
	            {
	                fichero.close();
	            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
          
      
    }

    private String loadBat(String path, String nameFile, String nameDir)
    {
        String psdReport;
        String pathPsd = path;
        psdReport = "";
        File fmkdir = new File(path + "\\" + nameDir);
        fmkdir.mkdir();
         
            
        saveFile((new StringBuilder()).append("psdparse.exe -e -w -d ").append(nameDir).append("\\assets ").append(nameFile).toString(), (new StringBuilder()).append(path).append("\\command.bat").toString());
        ProcessBuilder pb = new ProcessBuilder(new String[] {
            "cmd", "/c ", "command.bat"
        });
        File f = new File(path);
       
        pb.directory(f);
        Process start;
		try {
			start = pb.start();

	        java.io.InputStream is = start.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        for(String line = ""; (line = br.readLine()) != null;)
	        {
	            psdReport = (new StringBuilder()).append(psdReport).append(line).append("\n").toString();
	        }

	        f = new File((new StringBuilder()).append(path).append("\\command.bat").toString());
	        f.delete();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
		return psdReport;
    }

    private void getPlistContent(int rows, int cols, String chunk, String nameChunk)
    {
        Layer l = new Layer();
        StringTemplate plist = group.getInstanceOf("plistDict");
        StringTokenizer st = new StringTokenizer(chunk, "()\"\nx");
        StringTokenizer st2 = new StringTokenizer(st.nextToken().trim(), ",");
        String margins[] = new String[4];
        for(int i = 0; (i < margins.length); i++)
        {
            margins[i] = st2.nextToken().trim();
        }

        l.setPosx(Integer.parseInt(margins[1]));
        l.setPosy(Integer.parseInt(margins[0]));
        while(!st.nextToken().contains("channel")) ;
        String height = st.nextToken();
        height = height.substring(0, height.indexOf("row")).trim();
        l.setHeight(Integer.parseInt(height));
        String width = st.nextToken();
        width = width.substring(0, width.indexOf("col")).trim();
        l.setWidth(Integer.parseInt(width));
        st.nextToken();
        String name = st.nextToken().trim();
        l.setName((new StringBuilder()).append("assets/").append(name).append(".png").toString());
        vinetas.add(l);
    }

    private void copyFullImage(String path, String nameDir, String slash)
    {
        try
        {
        	//	nameDir+".psd.png";
        	
        	String origin = (new StringBuilder()).append(path).append(slash).append(nameDir).append(slash).append("assets").append(slash).append(nameDir).append(".psd.png").toString();
        	String target = (new StringBuilder()).append(path).append(slash).append(nameDir).append(slash).append("fullpage.png").toString();
        	   File afile =new File(origin);
        	   
        	   afile.renameTo( new File (target) );
        }
        catch(Exception ioe)
        {
            System.err.println("Error al Generar Copia");
        }
    }

    private List readGlosary(String GlosaryPath)
    {
        hash = new Hashtable();
        List ltmp = new ArrayList();
        try
        {
            FileReader fr = new FileReader(GlosaryPath);
            BufferedReader bf = new BufferedReader(fr);
            String sCadena;
            while((sCadena = bf.readLine()) != null) 
            {
                try
                {
                    GlosaryData data = new GlosaryData();
                    StringTokenizer st = new StringTokenizer(sCadena, "\t");
                    String sTmp = st.nextToken();
                    data.setKey(sTmp.trim());
                    data.setLabel(st.nextToken());
                    data.setText(st.nextToken());
                    ltmp.add(data);
                }
                catch(Exception e) { }
            }
        }
        catch(FileNotFoundException ex) { }
        catch(IOException ex) { }
        return ltmp;
    }
}
