package ntu.sd.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlController;

/**
* The IndexRegistry Class is for maintaining the functionality of indexing
* and is responsible of creating and holding all Indexers.
* <p>
* 	<b>Initialization:</b> Directory path for indexed data is optional (Default : ./tmp/index/)
* </p>
*/
public class IndexRegistry {
	static final Logger logger = Logger.getLogger(IndexRegistry.class.getName());
	private static String REG_POSTFIX = "reg"; 
	private String rootPath;
	private List<String> rootUrls = null;
	private List<Indexer> indexers;
	private String regPath;

	public IndexRegistry() {
		/* Deprecated path setting
		String workingDir = System.getProperty("user.dir");
		setRegistryPath(workingDir + File.separator + "tmp" + File.separator + "index");
		*/
		setRegistryPath(Indexer.getIndexStorageRootPath());
		load();
		indexers = new ArrayList<Indexer>();
	}
	
	public IndexRegistry(String path) {
		setRegistryPath(path);
		load();
		indexers = new ArrayList<Indexer>();
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	/**
	* This method checks incoming request, if it's already indexed, no further action will be performed.
	* If isForce flag is TRUE, then the index will be updated.
	* @param C CrawlController instance.
	* @param rootUrl request url string.
	* @param isForce flag, force index.
	* @return IndexRegistry self (this).
	* <p>
	* 	<b>Note:</b> If the write lock is present in the target directory, something may go wrong.
	* </p>
	*/
	public IndexRegistry register(CrawlController C, String rootUrl, boolean isForce) {
		logger.info("Received req " + rootUrl);
		if (add(rootUrl, isForce)) {
			try {
				//Indexer indexer = new Indexer(rootPath, "testing", rootUrl, this);
				Indexer indexer = new Indexer(rootUrl);
				indexers.add(indexer);
				C.addObserver(indexer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info(rootUrl + " register accepted.");
		}
		else {
			logger.info(rootUrl + " register denied.");
		}
		return this;
	}
	
	public boolean inquire(String source) {
		if (rootUrls != null) {
			if (rootUrls.contains(source)) {
				logger.info(source + " record found.");
				return true;
			}
		}
		return false;
	}
	
	public void sync() {
		// TODO
		// sync with index
	}
	
	public void save() {
		File reg = new File(regPath);
		try {
			if (!reg.exists()) {
				reg.createNewFile();
			}
	
			FileWriter fw = new FileWriter(reg.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String url : rootUrls) {
				bw.write(url + "\n");
			}
			bw.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	* This method discard all history records.
	*/
	public void flush() {
		rootUrls = null;
		File file = new File(rootPath);
		File[] files = file.listFiles();
		for (File f : files) {
			try {
				delete(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void delete(String rootUrl) {
		rootUrls.remove(rootUrl);
		File file = new File(urltoDirPath(rootUrl));
		try {
			delete(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void list() {
		for (String url : rootUrls) {
            logger.info(" -- " + url);
        }
	}
	
	public void showIndexing() {
		if (indexers == null) return;
		logger.info(String.format("now holds : %d", indexers.size()));
		int alive = 0;
		for (Indexer i : indexers) {
			if (i.getState().equals(Indexer.State.ACTIVE)) {
				alive++;
			}
		}
		logger.info(String.format("running : %d", alive));
	}
	
	public void shutdown() throws IOException {
		for (Indexer indexer : indexers) {
			indexer.close();
		}
		indexers = null;
		save();
	}
	
	/**
	* Unique directory name conversion.
	* @param rootUrl source string
	* @return converted string
	*/
	public String urltoDirPath(String rootUrl) {
		//return rootPath + File.separator + String.format("%040x", new BigInteger(1, rootUrl.getBytes()));
		return rootPath + "user" + "_" + rootUrl.replaceAll("\\:|\\.|\\?|\\/|\\&", "");
	}
	
	public boolean add(String source, boolean isForce) {
		boolean notRecorded;
		if (rootUrls == null) logger.error("registry not loaded!");
		if (!rootUrls.contains(source)) {
			rootUrls.add(source);
			notRecorded = true;
		}
		else {
			notRecorded = false;
			logger.info("Same request has indexed.");
		}
		
		if (isForce) {
			for (Indexer i : indexers) {
				if (i.getRootUrl().equals(source) && 
						i.getState().equals(Indexer.State.ACTIVE)) {
					logger.info("Detect same request for: " + source);
					return false;
				}
			}
			return true;
		}
		else {
			return notRecorded;
		}
		
	}
	
	private boolean setRegistryPath(String path) {
		rootPath = path;
		File tmpDir = new File(path);
		if (!tmpDir.exists()) {
			if(tmpDir.mkdir()) {    
				logger.info(String.format("Create directory: %s", path)); 
			}
			else {
				return false;
			}
		}
		regPath = path + File.separator + REG_POSTFIX;
		return true;
	}
	
	private void load() {
		File reg = new File(regPath);
		BufferedReader br = null;
		try {
			if (!reg.exists() || reg.isDirectory()) {
				reg.createNewFile();
			}
			br = new BufferedReader(new FileReader(regPath));
			String line;
			rootUrls = Collections.synchronizedList(new ArrayList<String>());
			while ((line = br.readLine()) != null) {
				rootUrls.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	* Method for file operation : delete
	* @param file remove directory (recursively) and files in it.
	* @return void
	* <p>
	* 	<b>Warn:</b> This method may cause damage to the file system.
	* </p>
	*/
	private static void delete(File file) throws IOException{
		if(file.isDirectory()){
			if(file.list().length==0){
				file.delete();
				logger.info("Directory is deleted : " + file.getAbsolutePath());
			}else{
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if(file.list().length==0){
					file.delete();
					logger.info("Directory is deleted : " + file.getAbsolutePath());
				}
			}
		}else{
			file.delete();
			logger.info("File is deleted : " + file.getAbsolutePath());
		}
	}
	
}
