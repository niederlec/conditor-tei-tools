package fr.conditor;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.saxonica.config.EnterpriseTransformerFactory;

import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.trans.XPathException;

public class SaxonUtil {

    public static void main(String[] args) throws IOException {
        String directory = args[0];
        String schema = args[1];

        File fDirectory = new File(directory);
        File fSchema = new File(schema);

        if (directory == null) {
            System.out.println("No source directory supplied");
            if (!fDirectory.exists() || !fDirectory.isDirectory()) {
                System.out.println("directory must be an existing directory");
                return;
            }
            return;
        }

        if (schema != null) {
            System.out.println("We will try to validate generated files thanks to " + schema);
            if (!fSchema.exists()) {
                System.out.println("schema must be a valid file");
                return;
            }
        }

        SaxonUtil saxonUtil = new SaxonUtil(fDirectory, fSchema);
        saxonUtil.fw.write("saxonUtil instanciated");

        File[] teiFiles =  fDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        saxonUtil.setSchema(fSchema);
        for (File teiFile : teiFiles) {
            if (teiFile.exists()) {
                saxonUtil.validate(teiFile);
            }
        }


    }



    /*
    public static void main(String[] args) throws IOException {
        System.out.println("debut main");


        String style = args[1];
        String schema = args[2];

        File fXslt = new File(style);
        File fSchema = new File(schema);

        if (style == null) {
            System.out.println("No xslt supplied");
            usage();
            return;
        }

        if (schema != null) {
            System.out.println("We will try to validate generated files thanks to " + schema);
            if (!fSchema.exists()) {
                System.out.println("schema must be a valid file");
                usage();
                return;
                //TODO Gérer proprement la mauvaise instanciation de la classe
            }
        }

        SaxonUtil saxonUtil = new SaxonUtil(fXslt, fSchema);
        saxonUtil.fw.write("saxonUtil instanciated");

        Path docObjectsPath = Paths.get(args[0]);
        Path errDocObjectsPath = Paths.get(args[0] + ".err");
        String strDocObjects = "";
        try {
            List<String> lines = Files.readAllLines(docObjectsPath);
            for (String line : lines) strDocObjects += line;
        } catch (IOException ex) {
            //TODO gérer erreur lecture fichier temporaire
        }

        JSONArray docObjects = new JSONArray(strDocObjects);
        for (int i = 0; i < docObjects.length(); i++) {
            JSONObject docObject = docObjects.getJSONObject(i);
            saxonUtil.fw.write("first doc object : "+docObject.toString());

            // ===> retrieve idIstex
            String idIstex = docObject.getString("idIstex");
            DocProperties docProps = new DocProperties(idIstex);

            // ===> retrieve arkIstex
            String arkIstex = (docObject.has("ark")) ? docObject.getString("ark") : "";
            if (arkIstex.length()>0) docProps.setArkIstex(arkIstex);

            // ===> retrieve output TEI path
            String corpusName = docObject.getString("corpusName");
            String corpusOutput = docObject.getString("corpusOutput");
            File teiOuptputDir = new File(corpusOutput
                    + "/" + idIstex.charAt(0)
                    + "/" + idIstex.charAt(1)
                    + "/" + idIstex.charAt(2)
                    + "/" + idIstex
                    + "/fulltext");
            if (!teiOuptputDir.exists()) teiOuptputDir.mkdirs();
            File teiOutputFile = new File(teiOuptputDir, idIstex + ".tei.xml");
            docProps.setTeiFile(teiOutputFile);

            // ===> retrieve raw TXT path if present
            String rawFulltextPath = "";
            JSONObject txtObject = SaxonUtil.getFormat(docObject, "fulltext", "text/plain");
            File cleanRawTxtFile = new File("/run/shm/li-2tei/"+idIstex+".txt.clean");
            if (txtObject != null) {
                rawFulltextPath = txtObject.getString("path");
                int resClean = saxonUtil.cleanTxt(rawFulltextPath,cleanRawTxtFile.getAbsolutePath(),idIstex);
                if (resClean == 0) docProps.setTxtFile(cleanRawTxtFile);
            }

            // retrievinge original XML
            JSONObject xmlMetadataObject = SaxonUtil.getFormat(docObject, "metadata", "/xml", true);

            if (xmlMetadataObject != null) {
                // ===> retrieving partOfSet path if present
                String issueXmlPath = "";
                if (xmlMetadataObject.has("partOfSet")) {
                    File partOfSetFile = new File(xmlMetadataObject.getString("partOfSet"));
                    if (partOfSetFile.exists()) docProps.setIssueXmlFile(partOfSetFile);
                }

                // ===> retrieving XML original path
                File inputFile = new File(xmlMetadataObject.getString("path"));
                if (!inputFile.exists()) {
                    docObject.put("errCode", 1);
                    docObject.put("errMessage", "Fichier XML inexistant : " + inputFile.getAbsolutePath());
                    errObjects.put(docObject);
                    docObjects.remove(i);
                    System.out.println("input (" + inputFile.getAbsolutePath() + ") must be a valid file or directory");
                    usage();
                } else {
                    docProps.setXmlFile(inputFile);
                    SaxonResult resXslt = saxonUtil.apply(docProps);
                    if (resXslt.code != 0) {
                        System.out.println("transfo KO");
                        docObject.put("errCode", 2);
                        docObject.put("errMessage", "Erreur de transformation en TEI du XML " + inputFile.getAbsolutePath() + " : " + resXslt.message); //TODO insérer message d'erreur complet
                        errObjects.put(docObject);
                        docObjects.remove(i);
                    } else {
                        JSONObject fulltextTEI = SaxonUtil.getFormat(docObject, "fulltext", "application/tei+xml");
                        if (fulltextTEI != null) {
                            fulltextTEI.put("pub2Tei",true);
                            fulltextTEI.remove("path");
                            fulltextTEI.put("path",teiOutputFile.getAbsolutePath());
                        } else {
                            fulltextTEI = new JSONObject();
                            fulltextTEI.put("mime", "application/tei+xml");
                            fulltextTEI.put("original", false);
                            fulltextTEI.put("path", teiOutputFile.getAbsolutePath());
                            fulltextTEI.put("pub2Tei",true);
                            if (docObject.has("fulltext")) {
                                docObject.getJSONArray("fulltext").put(fulltextTEI);
                            } else {
                                JSONArray fulltexts = new JSONArray();
                                fulltexts.put(fulltextTEI);
                                docObject.put("fulltext", fulltexts);
                            }
                        }


                    }
                }
            } // endif metadata XML

            if (cleanRawTxtFile.exists()) cleanRawTxtFile.delete();

        } // end docObjects iteration

        FileWriter outWriter = new FileWriter(docObjectsPath.toFile());
        outWriter.write(docObjects.toString());
        outWriter.close();

        FileWriter errWriter = new FileWriter(errDocObjectsPath.toFile());
        errWriter.write(errObjects.toString());
        errWriter.close();

        System.out.println(docObjects.toString());
        System.out.println(errObjects.toString());

    }


    public static JSONObject getFormat(JSONObject docObject, String dataType, String mimeType) {
        JSONObject jsonResult = null;
        if (docObject.has(dataType)) {
            JSONArray formats = docObject.getJSONArray(dataType);
            for (int j = 0; j < formats.length(); j++) {
                JSONObject mimeObject = formats.getJSONObject(j);
                if (mimeObject.getString("mime").endsWith(mimeType)) {
                    jsonResult = mimeObject;
                    break;
                }
            }
        }
        return jsonResult;
    }

    public static JSONObject getFormat(JSONObject docObject, String dataType, String mimeType, boolean isOriginal) {
        JSONObject jsonResult = null;
        if (docObject.has(dataType)) {
            JSONArray formats = docObject.getJSONArray(dataType);
            for (int j = 0; j < formats.length(); j++) {
                JSONObject mimeObject = formats.getJSONObject(j);
                if (mimeObject.getString("mime").endsWith(mimeType) && mimeObject.getBoolean("original") == isOriginal) {
                    jsonResult = mimeObject;
                    break;
                }
            }
        }
        return jsonResult;
    }


    public static void usage() {
        System.out.print("usage : java SaxonXSLT <docObjects> <styleSheet> <schema> (input can be file or dir)");
    }
*/

    private File fXslt, fSchema;
    private String outputPath;
    private String inputPath;
//    private Jedis jedis = null;
    private String sessionName = null;
    private String localModuleRoot = null;
    File logFile = new File("/tmp/saxon.log");
    FileWriter fw;

    public SaxonUtil(File xslt, File schema) throws IOException {
        this.fXslt = xslt;
        this.fSchema = schema;
        Map<String, String> env = System.getenv();
//        String jedisHost = env.get("REDIS_HOST") != null ? env.get("REDIS_HOST") : "localhost";
//        int jedisPort = env.get("REDIS_PORT") != null ? Integer.parseInt(env.get("REDIS_PORT")) : 6379;
        if (env.get("ISTEX_SESSION") != null) this.sessionName = env.get("ISTEX_SESSION");
        if (env.get("LOCAL_MODULEROOT") != null) this.localModuleRoot = env.get("LOCAL_MODULEROOT");
//        this.jedis = new Jedis(jedisHost, jedisPort);
        this.fw= new FileWriter(logFile);
        this.fw.write("end of constructor");
    }

    public int cleanTxt(String txtFile, String outTxtFile, String idIstex) {
        try {
            String scriptPath = "src/clean-txt.sh";
            if (this.localModuleRoot != null) scriptPath = this.localModuleRoot + "/" + scriptPath;
            String[] target = {scriptPath, txtFile, outTxtFile, idIstex};
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(target);
            proc.waitFor();
            StringBuffer output = new StringBuffer();
            BufferedReader outReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (outReader.ready()) outReader.readLine();
            while (errReader.ready()) errReader.readLine();
            return 0;
        } catch (Throwable t) {
            t.printStackTrace();
            return 1;
        }
    }

    public SaxonResult apply(DocProperties docProps) {

        this.outputPath = docProps.teiFile.getAbsolutePath();
        this.inputPath = (docProps.xmlFile.isDirectory()) ? docProps.xmlFile.getAbsolutePath() : docProps.xmlFile.getParentFile().getAbsolutePath();

        try {
            Templates pss = tryCache(fXslt);
            Transformer transformer = pss.newTransformer();
            ;
            if (!docProps.xmlFile.exists()) {
                throw new XPathException("Source file " + docProps.xmlFile.getAbsolutePath() + " not found");
            }

            //deactivation of DTD validation
            XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            xmlReader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String pid, String sid) throws SAXException {
                    return new InputSource(new ByteArrayInputStream(new byte[]{}));
                }
            });
            Source xmlSource = new SAXSource(xmlReader, new InputSource(new FileReader(docProps.xmlFile)));

            String sourceDirPath = docProps.xmlFile.getParentFile().getAbsolutePath();
            String relativeInputDir = sourceDirPath.substring(inputPath.length());

            System.out.println("transformation de " + docProps.xmlFile.getAbsolutePath() + " en " + docProps.teiFile.getAbsolutePath());

            if (docProps.idIstex != null && !docProps.idIstex.trim().equals("")) transformer.setParameter("idistex", docProps.idIstex);
            if (docProps.arkIstex != null && !docProps.arkIstex.trim().equals("")) transformer.setParameter("arkistex", docProps.arkIstex);
            if (docProps.txtFile != null && docProps.txtFile.exists()) transformer.setParameter("rawfulltextpath", docProps.txtFile);
            transformer.setParameter("datecreation", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

            transformer.transform(xmlSource, new StreamResult(docProps.teiFile));

            System.out.println("Transformation OK, consulter le fichier " + docProps.teiFile);

//            jedis.hincrBy("Module:" + sessionName + ":li-2tei", "out", 1);

            return new SaxonResult(0, null);

        } catch (Exception err) {
//        	System.out.println("Transformation KO sur le fichier "+in);
//        	System.err.println("Echec XSLT sur le fichier "+source);
//            System.err.println(err.getMessage());
//            err.printStackTrace();

            //TODO gérer proprement les erreurs de tranformation (logs, etc.)
            return new SaxonResult(1, err.getMessage());
        }
    }

    public class SaxonResult {
        public final int code;
        public final String message;

        public SaxonResult(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public int validate(File fileToValidate) {
        try {
            System.setProperty("http.proxyHost", "proxyout.inist.fr");
            System.setProperty("http.proxyPort", "8080");
            System.setProperty("https.proxyHost", "proxyout.inist.fr");
            System.setProperty("https.proxyPort", "8080");

            System.setProperty("javax.xml.transform.TransformerFactory",
                    "com.saxonica.config.EnterpriseTransformerFactory");
            TransformerFactory factory = TransformerFactory.newInstance();
//            <attribute name="http://saxon.sf.net/feature/licenseFileLocation" value="${licenseFile}"/>
            factory.setAttribute("http://saxon.sf.net/feature/licenseFileLocation","saxon.lic");
            factory.setAttribute(FeatureKeys.SCHEMA_VALIDATION, new Integer(
                    Validation.STRICT));
            StreamSource schema = new StreamSource(fSchema.toURI().toString());
            ((EnterpriseTransformerFactory) factory).addSchema(schema);
            Transformer trans = factory.newTransformer();
            StreamSource source = new StreamSource(fileToValidate.toURI().toString());
            SAXResult sink = new SAXResult(new DefaultHandler());
            trans.transform(source, sink);

        } catch (TransformerException err) {
            System.out.println("Validation KO sur le fichier " + fileToValidate.getAbsolutePath());
            System.err.println("Erreur de validation sur le fichier " + fileToValidate.getAbsolutePath());
            System.err.println(err.getMessage());
            return 1;
        }
        System.out.println("Validation OK sur le fichier " + fileToValidate.getAbsolutePath());
        return 0;
    }


    /**
     * Maintain prepared stylesheets in memory for reuse
     */

    private synchronized static Templates tryCache(File xslFile) throws TransformerException {
        // String path = getServletContext().getRealPath(xslPath);
        if (!xslFile.exists()) {
            throw new XPathException("Stylesheet " + xslFile.getAbsolutePath() + " not found");
        }
        String path = xslFile.getAbsolutePath();

        Templates x = (Templates) cache.get(path);
        if (x == null) {
            TransformerFactory factory = TransformerFactory.newInstance();
            x = factory.newTemplates(new StreamSource(new File(path)));
            cache.put(path, x);
        }
        return x;
    }

    public File getSchema() {
        return fSchema;
    }

    public void setSchema(File fSchema) {
        this.fSchema = fSchema;
    }

    public boolean hasSchema() {
        return (this.fSchema != null);
    }

    private static HashMap<String, Templates> cache = new HashMap<String, Templates>(20);

}
