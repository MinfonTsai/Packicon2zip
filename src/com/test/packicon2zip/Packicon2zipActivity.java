package com.test.packicon2zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Packicon2zipActivity extends Activity {
    /** Called when the activity is first created. */
	
	List<LauncherItem> lvalue; 
	PackageManager pkgMgt;
	Toast toast;
    private LauncherAdapter mMyAdapter;  
    String sd_path="/sdcard/DCIM/ICONS/"; 
    
    static String regEx = "[\u4e00-\u9fa5]";    
    static Pattern pat = Pattern.compile(regEx);   
    boolean Is_chinese;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.main);
        
        ZipUtil zp= new ZipUtil();
        
        try {
			addLauncher();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {
			zp.zipFolder( "/sdcard/DCIM/ICONS","/sdcard/ICONS.zip" );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        toast = Toast.makeText(Packicon2zipActivity.this, "本機軟體清單打包完成", Toast.LENGTH_SHORT);
   	    toast.show();
   	 
   	    this.finish();
    }
    
    
 	public void addLauncher() throws Exception{ 
    	
    	String packageName1=null; 
    	String className1=null;
   		lvalue = new ArrayList<LauncherItem>(); 
   		pkgMgt = this.getPackageManager(); 
   		PackageManager pm = this.getPackageManager();
   	//	PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);
   	 
   		delDir(sd_path);
   		
   			 
   		//to query all launcher & load into List<> 
   		Intent it = new Intent(Intent.ACTION_MAIN); 
   		it.addCategory(Intent.CATEGORY_LAUNCHER); 
   		List<ResolveInfo> ra =pkgMgt.queryIntentActivities(it,0); 
   		for(int i=0;i<ra.size();i++)
   		{ 
   			ActivityInfo ai = ra.get(i).activityInfo; 
	   		//String ainfo = ai.toString(); 
	   		Drawable icon = ai.loadIcon(pkgMgt); 
	   		String label = ai.loadLabel(pkgMgt).toString(); 
	   		String label_1;
	   		String label_2;
	   		
	   		label_1 = new String(label.getBytes(),"UTF-8");  //必須處理, 否則有中文亂碼
	   		Is_chinese = isContainsChinese(label_1);   // 檔名有中文	   		
	   		label_2 = label_1.replace( ' ','_');   // 用 - 取代空白字元
	   		
	   	//	String label_hex;
	   	//	String label_ch;
	   		
	   		if( Is_chinese == true )             //把中文名先轉成 Hex 16進位碼, 到另一處再轉回來
	   		{
	   			label_2 =  "@"+Hex_encode( label_2 );
	   			//label_hex =  Hex_encode( label_2 );
	   			// label_ch = Hex_decode(label_hex);
	   		}
	   		
	   		// 儲存icon 為PNG 圖片檔
	   		BitmapDrawable bd = (BitmapDrawable) icon;
	   		Bitmap bm = bd.getBitmap();   
	   		
	   		// 加上 code , 建立一資料夾
	   		
	        File sdfile=new File(sd_path); 
	        if(!sdfile.exists()) 
	         sdfile.mkdir();

	   		File fi = new File(sd_path + label_2 +"_"+ai.applicationInfo.packageName+"_"+ai.name+".jpg");   
	        BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(fi));
         	bm.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bos.flush();
     	   	bos.close();

     	  
     	   	// Get Component Name
	   		ComponentName c = new ComponentName(ai.applicationInfo.packageName,ai.name); 
	   		packageName1 = ai.applicationInfo.packageName;   // Get Package Name
	   		
	   		//pi = getPackageManager().getPackageInfo( ai.applicationInfo.packageName, i);
	   		List<ResolveInfo> apps = pm.queryIntentActivities(it, i);
	   		ResolveInfo ri = apps.listIterator(i).next(); 
	 	 	if (ri != null ) 
		   	 className1 =ri.activityInfo.name;     // Get Class Name
			    	 
		    LauncherItem item = new LauncherItem(icon,label_2,c,packageName1,className1); 
			lvalue.add(item); 	 
   		 } 
      	   
   	 } 
 	//=============================================================== 判斷字串中有無中文字元
 	public static boolean isContainsChinese(String str)      
    {     
        Matcher matcher = pat.matcher(str);      
        boolean flg = false;   
        if (matcher.find())    {     
            flg = true;    
        }      
        return flg;      
    }   
 	//=================================================================================  
 	private static String hexString="0123456789ABCDEF"; 
 	 	// 將字串編碼成16進制數位,適用於所有字元（包括中文） 
 	 public static String Hex_encode(String str) 
 	{ 
 	//根據預設編碼獲取位元組陣列 
 	byte[] bytes=str.getBytes(); 
 	StringBuilder sb=new StringBuilder(bytes.length*2); 
 	//將位元組陣列中每個位元組拆解成2位元16進制整數 
 	for(int i=0;i<bytes.length;i++) 
 	{ 
 	sb.append(hexString.charAt((bytes[i]&0xf0)>>4)); 
 	sb.append(hexString.charAt((bytes[i]&0x0f)>>0)); 
 	} 
 	return sb.toString(); 
 	} 

 	/* 
 	* 將16進制數位解碼成字串,適用於所有字元（包括中文） 
 	*/ 
 	public static String Hex_decode(String bytes) 
 	{ 
 	ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2); 
 	//將每2位元16進制整數組裝成一個位元組 
 	for(int i=0;i<bytes.length();i+=2) 
 	baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1)))); 
 	return new String(baos.toByteArray()); 
 	} 

	 	public void delDir(String path) { 
	 		File dir=new File(path); 
	 		if(dir.exists()) { 
		 		File[] tmp=dir.listFiles(); 
		 		for(int i=0;i<tmp.length;i++) { 
				 		if(tmp[i].isDirectory()) { 
				 			delDir(path+"/"+tmp[i].getName()); 
				 		} 
				 		else { 	tmp[i].delete(); } 
		 		} 
	 			dir.delete(); 
	 		} 
	 	} 
//=================================================================================  
  	public class LauncherItem {
     	Drawable icon;
      	String name;
      	ComponentName component;
      	String package_name;
      	String class_name;
     	LauncherItem(Drawable d, String s,ComponentName cn,String pn, String clsn){
    	icon = d;
    	name = s;
    	component = cn;
    	package_name = pn;
    	class_name = clsn;
      	}
	};
	//=================================================================================
	  private class LauncherAdapter extends BaseAdapter{  
	    	
	    	Activity activity;
	    	public LauncherAdapter(Activity a){
		   		activity = a;
	   		}
	    //    @Override  
	        public int getCount() {  
	        
	        	return lvalue.size();
	        }  
	     //   @Override  
	        public Object getItem(int arg0) {  
	            return arg0;  
	        }  
	    //    @Override  
	        public long getItemId(int position) {  
	            return position;  
	        }  
	     //    @Override  
	        public View getView(int position, View convertView, ViewGroup parent) {  
	         /*
	         	TextView mTextView = new TextView(getApplicationContext());  
	            mTextView.setText("BaseAdapterDemo");  
	            mTextView.setTextColor(Color.RED);  
	            return mTextView;  
	         */  
	            return composeItem(position);
	        }  
	           //--------------------------------       
	    	public View composeItem(int position){
		   		LinearLayout layout = new LinearLayout(activity);
		   		layout.setOrientation(LinearLayout.HORIZONTAL);
		   		ImageView iv = new ImageView(activity);
		   		iv.setImageDrawable(lvalue.get(position).icon);
		   		layout.addView(iv);
		   	
		   		TextView tv = new TextView(activity);
		   		tv.setText(lvalue.get(position).name);
		   		//tv.setTextColor(Color.RED);
		     	tv.setPadding(10, 5, 0, 0);
		   		layout.addView(tv);
		   		
		   		TextView tv2 = new TextView(activity);
		   		tv2.setText(lvalue.get(position).package_name+"/"+lvalue.get(position).class_name);
		   		//tv.setTextColor(Color.RED);
		     	tv2.setPadding(10, 35, 0, 0);
		   		layout.addView(tv2);
		   			   		   		
		   		return layout;
	   		}
	          //-----------------------------------------------    	
	    }  // end of class LauncherAdapter       

		//=================================================================================	  
	  /**
	     * Android Zip压缩解压缩
	      */
	    public final class ZipUtil {
	    	private ZipUtil(){
	    	}
	    	
	    	/**
	    	 * 取得压缩包中的 文件列表(文件夹,文件自选)
	    	 * @param zipFileString		压缩包名字
	    	 * @param bContainFolder	是否包括 文件夹
	    	 * @param bContainFile		是否包括 文件
	    	 * @return
	    	 * @throws Exception
	    	 */
	    	public java.util.List<java.io.File> getFileList(String zipFileString, boolean bContainFolder, boolean bContainFile)throws Exception {
	    		java.util.List<java.io.File> fileList = new java.util.ArrayList<java.io.File>();
	    		java.util.zip.ZipInputStream inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFileString));
	    		java.util.zip.ZipEntry zipEntry;
	    		String szName = "";
	    		
	    		while ((zipEntry = inZip.getNextEntry()) != null) {
	    			szName = zipEntry.getName();
	    		
	    			if (zipEntry.isDirectory()) {
	    		
	    				// get the folder name of the widget
	    				szName = szName.substring(0, szName.length() - 1);
	    				java.io.File folder = new java.io.File(szName);
	    				if (bContainFolder) {
	    					fileList.add(folder);
	    				}
	    		
	    			} else {
	    				java.io.File file = new java.io.File(szName);
	    				if (bContainFile) {
	    					fileList.add(file);
	    				}
	    			}
	    		}//end of while
	    		
	    		inZip.close();
	    		
	    		return fileList;
	    	}
	    	
	    	 /**
	    	 * 压缩文件,文件夹
	    	 * 
	    	 * @param srcFilePath	要压缩的文件/文件夹名字
	    	 * @param zipFilePath	指定压缩的目的和名字
	    	 * @throws Exception
	    	 */
	    	public void zipFolder(String srcFilePath, String zipFilePath)throws Exception {
	    		//创建Zip包
	    		java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFilePath));
	    		
	    		//打开要输出的文件
	    		java.io.File file = new java.io.File(srcFilePath);

	    		//压缩
	    		zipFiles(file.getParent()+java.io.File.separator, file.getName(), outZip);
	    		
	    		//完成,关闭
	    		outZip.finish();
	    		outZip.close();
	    	
	    	}//end of func
		    /**
	    	 * 压缩文件
	    	 * @param folderPath
	    	 * @param filePath
	    	 * @param zipOut
	    	 * @throws Exception
	    	 */
	    	private void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)throws Exception{
	    		if(zipOut == null){
	    			return;
	    		}
	    		
	    		java.io.File file = new java.io.File(folderPath+filePath);
	    		
	    		//判断是不是文件
	    		if (file.isFile()) {

	    			java.util.zip.ZipEntry zipEntry =  new java.util.zip.ZipEntry(filePath);
	    			java.io.FileInputStream inputStream = new java.io.FileInputStream(file);
	    			zipOut.putNextEntry(zipEntry);
	    			
	    			int len;
	    			byte[] buffer = new byte[4096];
	    			
	    			while((len=inputStream.read(buffer)) != -1)
	    			{
	    				zipOut.write(buffer, 0, len);
	    			}
	    			
	    			zipOut.closeEntry();
	    		}
	    		else {
	    			
	    			//文件夹的方式,获取文件夹下的子文件
	    			String fileList[] = file.list();
	    			
	    			//如果没有子文件, 则添加进去即可
	    			if (fileList.length <= 0) {
	    				java.util.zip.ZipEntry zipEntry =  new java.util.zip.ZipEntry(filePath+java.io.File.separator);
	    				zipOut.putNextEntry(zipEntry);
	    				zipOut.closeEntry();				
	    			}
	    			
	    			//如果有子文件, 遍历子文件
	    			for (int i = 0; i < fileList.length; i++) {
	    				zipFiles(folderPath, filePath+java.io.File.separator+fileList[i], zipOut);
	    			}//end of for
	    	
	    		}//end of if
	    		
	    	}//end of func
	    	
	    	public void finalize() throws Throwable {
	    		
	    	}
	    }
	    
	   

    }
