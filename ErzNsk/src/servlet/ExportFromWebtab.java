package servlet;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import model.handsontable.ListWeb;
import oracle.ConnectionPoolOracle;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.tomcat.jni.Time;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * Servlet implementation class ActionServlet
 * �� ������ ������ ����������, ����� ����� ������� ������ ������������ p02
 */

@WebServlet("/ExportFromWebtab")
public class ExportFromWebtab extends HttpServlet {
private static final long serialVersionUID = 1L;
   
 
    public ExportFromWebtab() {
        // TODO Auto-generated constructor stub
    }


  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  // 1. get received JSON data from request
      BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
      String json = "";
      if(br != null){ json = br.readLine(); }
      /*
       * ��� ����������� ����������� �������� ��� �������� � ��������� � ������ ��������� ������������ ����������� ��������
       * ���������� ��� ���
       */
      String fg = URLEncoder.encode(json, "Cp1251");
      String fg2 = URLDecoder.decode(fg, "UTF-8");
      fg2 = fg2.replaceAll("\\?", "�").replaceAll("[^A-Za-z�-��-�0-9- -.-:-\\]-\\[-{-}-�-]", "");
      // 2. initiate jackson mapper
      ObjectMapper mapper = new ObjectMapper();
      // 3. Convert received JSON to Article
      ListWeb article = mapper.readValue(fg2, ListWeb.class);
      // String user = article.getGouser().replaceAll("gouser=zapros=", "");
      
      ArrayList<ArrayList<String>> ls = null;
      try
      {
    	addHead(article.getList1());
		ls = processing(article.getList1(),article.getList2(),article.getList3(),article.getGouser());
      } catch ( Exception e) {
		e.printStackTrace();
      }
      
      /*
       * �������� �� ��������� �������
       * ���������� �������� �� �������������� �� ������� ��� �� ��������� ����������� ��� ������������ ������. ���� if(f.get(0).trim().equalsIgnoreCase(f3.get(0).trim()) &&  f3.get(19).trim().equalsIgnoreCase("0")) ����� �� ��� =1
       */
      ArrayList<ArrayList<String>> inf = sonar(ls); 
      
      Map<String, ArrayList<ArrayList<String>>> ind = new LinkedHashMap<String, ArrayList<ArrayList<String>>>();
      ind.put("list1", ls);
      ind.put("info", inf);
      
      json= new Gson().toJson(ind);   
      response.setContentType("application/json");
	  response.setCharacterEncoding("UTF-8");
	  response.getWriter().write(json.toString());
	    
      
	  
  }

  
 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 {
  // TODO Auto-generated method stub
  
 }
 
 private ArrayList<ArrayList<String>> processing(List listWeb1,List listWeb2,List listWeb3,String kluch) throws Exception 
 {
	 ResultSet rs = null;
     Connection conn = null;
     PreparedStatement stmt = null;
	 DataSource dataSource = ConnectionPoolOracle.setUp();
     ConnectionPoolOracle.printStatus();
     conn = dataSource.getConnection();
     ConnectionPoolOracle.printStatus();
	 
	 ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
	 ArrayList<String> f3,f2,f2m;
	 
	 try{ 
		 // ��������� ���������� ���, ��� �������� ������ ���� � ������ zp1 ������ ��� ��� ����� ������� ��� (�.�. ������� enp_1)
		 ArrayList<String> badInsideEnp = uniqueGUID(listWeb3);
		 
		 
		 for (int i2 = 0; i2 < listWeb1.size(); i2++)
		 {
			 ArrayList<String> f= (ArrayList<String>)listWeb1.get(i2);
			 if(i2 > 0)	chancheNUL(f);
			 
			 if(!badInsideEnp.contains(f.get(0).trim()))
			 {
						 // �������� ��� ����� � ������� ����������� d2 � ����� ����
						 String d_d2 = "";
						 for (int i = 0; i < listWeb2.size(); i++)
						 {
							 ArrayList<String> gh= (ArrayList<String>)listWeb2.get(i);
							 if(f.get(0).trim().equalsIgnoreCase(gh.get(26).trim()))
							 {
								 d_d2 =  gh.get(59);
							 }
							 
						 }
						 
						 
						 for (int j2 = 0; j2 < listWeb3.size(); j2++)
					 	{
					 			f3= (ArrayList<String>)listWeb3.get(j2);
					 			
					 			Date date= null;
					 			if(j2>0){		/*System.out.println("LLLLLLLLLLLL "+	parseDate(f3.get(11))	);  */date = parseDate(f3.get(11),"dd.MM.yyyy");		}
					 			
					 			// ��� ��, �13 ��� (d_d2 ����� � d13 > �������), �����,npp,  (��� -�������� ������������ � ������ (POL) �� �� ��)  
					 			if(f.get(0).trim().equalsIgnoreCase(f3.get(0).trim()) &&  f3.get(19).trim().equalsIgnoreCase("0") && ( f3.get(11).trim().equalsIgnoreCase("") || (	!d_d2.trim().equalsIgnoreCase("") && date.after(new Date()) )  )  && f3.get(12).trim().equalsIgnoreCase("50000"))
					 			{
					 				
					 				// ������ �������� ����� ������ � ���� 
					 				if(	fun(f3.get(0).trim(),f3.get(6).trim(),stmt,rs,conn)== 0	)
					 				{
					 					// �����
						 				f.set(10, "50000");
						 				// �12
						 				//f.set(8, f3.get(10));	
						 				f.set(8, parseStringDate(f3.get(10)) );
						 			    // d13
						 				DateFormat inputFormat = new SimpleDateFormat("dd.MM.YYYY");
					 					if(kluch.contains("A08P02today")){Date now = new Date();	f.set(9,inputFormat.format(now));	}
						 					
						 					if(kluch.contains("A08P02howINsmo"))
						 					{
						 						for (int k = 0; k < listWeb2.size(); k++)
						 						{
						 							f2m = (ArrayList<String>) listWeb2.get(k);
						 								//PERSON_DATECHANGE
						 								if(f2m.get(26).trim().equals(f.get(0).trim())){	 f.set(9, f2m.get(13));	}
													
												}
						 					}	
						 				// ���
						 				f.set(7, f3.get(9).trim());
						 				// ��� ������
						 				f.set(11, f3.get(13).trim());
						 				//	� ������
						 				f.set(12, f3.get(14).trim());
						 				// ��
						 				f2m = (ArrayList<String>) listWeb1.get(0);
						 				f2m.set(20, "BIRTHDAY");
						 				f.set(20, f3.get(21).trim());
						 				// ���
						 				f2m = (ArrayList<String>) listWeb1.get(0);
						 				f2m.set(19, "SEX");
						 				f.set(19, f3.get(22).trim());
						 				
						 				/**************************************************/
						 				for (int j3 = 0; j3 < listWeb2.size(); j3++)
			 		 				   {
			 								f2= (ArrayList<String>)listWeb2.get(j3);
			 								if(	f.get(0).trim().equalsIgnoreCase(f2.get(26).trim())	&& (f2.get(11).trim().equalsIgnoreCase("0") || f2.get(11).trim().equalsIgnoreCase("-1")	)	)	// c��������� �� ����������� ���
			 								{
			 									// 	��������� ��� 
			 					 				if(f2.get(39).trim().equals(""))	{ f.set(3, f2.get(26));	} else {	f.set(3, f2.get(39));	}
			 					 				// �������
			 					 				 f.set(4, f2.get(0));
			 					 				 // ���
			 					 				f.set(5, f2.get(1));
			 					 				// ��������
			 					 				f.set(6, f2.get(2));
			 					 				// ����� ��������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(13, "PassDOC");
			 					 				f.set(13, f2.get(8));
			 					 				// ����� ��������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(14, "NumDOC");
			 					 				f.set(14, f2.get(9));
			 					 				// ���� ������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(15, "DateDOC");
			 					 				f.set(15, f2.get(32));
			 					 				// ����� ��������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(16, "BORN");
			 					 				f.set(16, f2.get(31));
			 					 				// �����������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(17, "GOVER");
			 					 				f.set(17, parseGoverment(f2.get(33)	)	);
			 					 				 // ���  ��������� ���������
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(18, "CODEDOC");
			 					 				f.set(18, f2.get(17));
			 					 				 // d2
			 					 				f2m = (ArrayList<String>) listWeb1.get(0);
			 					 				f2m.set(22, "D2");
			 					 				f.set(22, f2.get(59));
						 					}
						 				}
					 				}
					 				else
					 				{
					 					f.set(7, "��� �� ������ �������� �� LINKSMO <= 0");
					 				}	
					 			}
					 				
					 	}
						 // j2>0 (���������� �����)
							 	if(i2 > 0 && f.get(10).equals("") && !f.get(0).contains("A08") && !f.get(7).equals("��� �� ������ �������� �� LINKSMO <= 0") ){	f.set(6, "��� ��� ������ ZP1 ���  ��� npp =0 ( ����� �� ����� 50000 ��� d13 ����������� ��� �� �� ) ��� d13 ���������� �� ����������� ���� ");	}   
			 } else{	if(i2 > 0){	f.set(6,"� ������ ��� ������� ���");	}	}
			 
			 list.add(f);
		 }
		
 } 	
	finally
	{
     if (stmt != null) {  stmt.close();  }
     if (conn != null)
     {
         conn.close();
         System.out.println("After finaly. Idle pool.");
     }
	}
	ConnectionPoolOracle.printStatus();
	 
	 return list;
 }
 
 /*
  * ������ �����������
  * � ���� ���� �������. � ����� �� ������ ������ ��� ���� � ������� �� ��������� ������� �.�. ������� �� ���� ���������� �� ��3���� (����)
  */
 private String parseGoverment(String str)
 {
	 if(str.trim().equalsIgnoreCase("004")) str="AFG";
	 if(str.trim().equalsIgnoreCase("008")) str="ALB";
	 if(str.trim().equalsIgnoreCase("010")) str="ATA";
	 if(str.trim().equalsIgnoreCase("012")) str="DZA";
	 if(str.trim().equalsIgnoreCase("016")) str="ASM";
	 if(str.trim().equalsIgnoreCase("020")) str="AND";
	 if(str.trim().equalsIgnoreCase("024")) str="AGO";
	 if(str.trim().equalsIgnoreCase("028")) str="ATG";
	 if(str.trim().equalsIgnoreCase("031")) str="AZE";
	 if(str.trim().equalsIgnoreCase("31")) str="AZE";
	 if(str.trim().equalsIgnoreCase("032")) str="ARG";
	 if(str.trim().equalsIgnoreCase("036")) str="AUS";
	 if(str.trim().equalsIgnoreCase("040")) str="AUT";
	 if(str.trim().equalsIgnoreCase("044")) str="BHS";
	 if(str.trim().equalsIgnoreCase("048")) str="BHR";
	 if(str.trim().equalsIgnoreCase("050")) str="BGD";
	 if(str.trim().equalsIgnoreCase("051")) str="ARM";
	 if(str.trim().equalsIgnoreCase("51")) str="ARM";
	 if(str.trim().equalsIgnoreCase("052")) str="BRB";
	 if(str.trim().equalsIgnoreCase("056")) str="BEL";
	 if(str.trim().equalsIgnoreCase("060")) str="BMU";
	 if(str.trim().equalsIgnoreCase("064")) str="BTN";
	 if(str.trim().equalsIgnoreCase("068")) str="BOL";
	 if(str.trim().equalsIgnoreCase("070")) str="BIH";
	 if(str.trim().equalsIgnoreCase("072")) str="BWA";
	 if(str.trim().equalsIgnoreCase("074")) str="BVT";
	 if(str.trim().equalsIgnoreCase("076")) str="BRA";
	 if(str.trim().equalsIgnoreCase("084")) str="BLZ";
	 if(str.trim().equalsIgnoreCase("086")) str="IOT";
	 if(str.trim().equalsIgnoreCase("090")) str="SLB";
	 if(str.trim().equalsIgnoreCase("092")) str="VGB";
	 if(str.trim().equalsIgnoreCase("096")) str="BRN";
	 if(str.trim().equalsIgnoreCase("100")) str="BGR";
	 if(str.trim().equalsIgnoreCase("104")) str="MMR";
	 if(str.trim().equalsIgnoreCase("108")) str="BDI";
	 if(str.trim().equalsIgnoreCase("112")) str="BLR";
	 if(str.trim().equalsIgnoreCase("116")) str="KHM";
	 if(str.trim().equalsIgnoreCase("120")) str="CMR";
	 if(str.trim().equalsIgnoreCase("124")) str="CAN";
	 if(str.trim().equalsIgnoreCase("132")) str="CPV";
	 if(str.trim().equalsIgnoreCase("136")) str="CYM";
	 if(str.trim().equalsIgnoreCase("140")) str="CAF";
	 if(str.trim().equalsIgnoreCase("144")) str="LKA";
	 if(str.trim().equalsIgnoreCase("148")) str="TCD";
	 if(str.trim().equalsIgnoreCase("152")) str="CHL";
	 if(str.trim().equalsIgnoreCase("156")) str="CHN";
	 if(str.trim().equalsIgnoreCase("158")) str="TWN";
	 if(str.trim().equalsIgnoreCase("162")) str="CXR";
	 if(str.trim().equalsIgnoreCase("166")) str="CCK";
	 if(str.trim().equalsIgnoreCase("170")) str="COL";
	 if(str.trim().equalsIgnoreCase("174")) str="COM";
	 if(str.trim().equalsIgnoreCase("175")) str="MYT";
	 if(str.trim().equalsIgnoreCase("178")) str="COG";
	 if(str.trim().equalsIgnoreCase("180")) str="COD";
	 if(str.trim().equalsIgnoreCase("184")) str="COK";
	 if(str.trim().equalsIgnoreCase("188")) str="CRI";
	 if(str.trim().equalsIgnoreCase("191")) str="HRV";
	 if(str.trim().equalsIgnoreCase("192")) str="CUB";
	 if(str.trim().equalsIgnoreCase("196")) str="CYP";
	 if(str.trim().equalsIgnoreCase("203")) str="CZE";
	 if(str.trim().equalsIgnoreCase("204")) str="BEN";
	 if(str.trim().equalsIgnoreCase("208")) str="DNK";
	 if(str.trim().equalsIgnoreCase("212")) str="DMA";
	 if(str.trim().equalsIgnoreCase("214")) str="DOM";
	 if(str.trim().equalsIgnoreCase("218")) str="ECU";
	 if(str.trim().equalsIgnoreCase("222")) str="SLV";
	 if(str.trim().equalsIgnoreCase("226")) str="GNQ";
	 if(str.trim().equalsIgnoreCase("231")) str="ETH";
	 if(str.trim().equalsIgnoreCase("232")) str="ERI";
	 if(str.trim().equalsIgnoreCase("233")) str="EST";
	 if(str.trim().equalsIgnoreCase("234")) str="FRO";
	 if(str.trim().equalsIgnoreCase("238")) str="FLK";
	 if(str.trim().equalsIgnoreCase("239")) str="SGS";
	 if(str.trim().equalsIgnoreCase("242")) str="FJI";
	 if(str.trim().equalsIgnoreCase("246")) str="FIN";
	 if(str.trim().equalsIgnoreCase("248")) str="ALA";
	 if(str.trim().equalsIgnoreCase("250")) str="FRA";
	 if(str.trim().equalsIgnoreCase("254")) str="GUF";
	 if(str.trim().equalsIgnoreCase("258")) str="PYF";
	 if(str.trim().equalsIgnoreCase("260")) str="ATF";
	 if(str.trim().equalsIgnoreCase("262")) str="DJI";
	 if(str.trim().equalsIgnoreCase("266")) str="GAB";
	 if(str.trim().equalsIgnoreCase("268")) str="GEO";
	 if(str.trim().equalsIgnoreCase("270")) str="GMB";
	 if(str.trim().equalsIgnoreCase("275")) str="PSE";
	 if(str.trim().equalsIgnoreCase("276")) str="DEU";
	 if(str.trim().equalsIgnoreCase("288")) str="GHA";
	 if(str.trim().equalsIgnoreCase("292")) str="GIB";
	 if(str.trim().equalsIgnoreCase("296")) str="KIR";
	 if(str.trim().equalsIgnoreCase("300")) str="GRC";
	 if(str.trim().equalsIgnoreCase("304")) str="GRL";
	 if(str.trim().equalsIgnoreCase("308")) str="GRD";
	 if(str.trim().equalsIgnoreCase("312")) str="GLP";
	 if(str.trim().equalsIgnoreCase("316")) str="GUM";
	 if(str.trim().equalsIgnoreCase("320")) str="GTM";
	 if(str.trim().equalsIgnoreCase("324")) str="GIN";
	 if(str.trim().equalsIgnoreCase("328")) str="GUY";
	 if(str.trim().equalsIgnoreCase("332")) str="HTI";
	 if(str.trim().equalsIgnoreCase("334")) str="HMD";
	 if(str.trim().equalsIgnoreCase("336")) str="VAT";
	 if(str.trim().equalsIgnoreCase("340")) str="HND";
	 if(str.trim().equalsIgnoreCase("344")) str="HKG";
	 if(str.trim().equalsIgnoreCase("348")) str="HUN";
	 if(str.trim().equalsIgnoreCase("352")) str="ISL";
	 if(str.trim().equalsIgnoreCase("356")) str="IND";
	 if(str.trim().equalsIgnoreCase("360")) str="IDN";
	 if(str.trim().equalsIgnoreCase("364")) str="IRN";
	 if(str.trim().equalsIgnoreCase("368")) str="IRQ";
	 if(str.trim().equalsIgnoreCase("372")) str="IRL";
	 if(str.trim().equalsIgnoreCase("376")) str="ISR";
	 if(str.trim().equalsIgnoreCase("380")) str="ITA";
	 if(str.trim().equalsIgnoreCase("384")) str="CIV";
	 if(str.trim().equalsIgnoreCase("388")) str="JAM";
	 if(str.trim().equalsIgnoreCase("392")) str="JPN";
	 if(str.trim().equalsIgnoreCase("398")) str="KAZ";
	 if(str.trim().equalsIgnoreCase("400")) str="JOR";
	 if(str.trim().equalsIgnoreCase("404")) str="KEN";
	 if(str.trim().equalsIgnoreCase("408")) str="PRK";
	 if(str.trim().equalsIgnoreCase("410")) str="KOR";
	 if(str.trim().equalsIgnoreCase("414")) str="KWT";
	 if(str.trim().equalsIgnoreCase("417")) str="KGZ";
	 if(str.trim().equalsIgnoreCase("418")) str="LAO";
	 if(str.trim().equalsIgnoreCase("807")) str="MKD";
	 if(str.trim().equalsIgnoreCase("818")) str="EGY";
	 if(str.trim().equalsIgnoreCase("826")) str="GBR";
	 if(str.trim().equalsIgnoreCase("831")) str="GGY";
	 if(str.trim().equalsIgnoreCase("832")) str="JEY";
	 if(str.trim().equalsIgnoreCase("833")) str="IMN";
	 if(str.trim().equalsIgnoreCase("834")) str="TZA";
	 if(str.trim().equalsIgnoreCase("840")) str="USA";
	 if(str.trim().equalsIgnoreCase("850")) str="VIR";
	 if(str.trim().equalsIgnoreCase("854")) str="BFA";
	 if(str.trim().equalsIgnoreCase("858")) str="URY";
	 if(str.trim().equalsIgnoreCase("860")) str="UZB";
	 if(str.trim().equalsIgnoreCase("862")) str="VEN";
	 if(str.trim().equalsIgnoreCase("876")) str="WLF";
	 if(str.trim().equalsIgnoreCase("882")) str="WSM";
	 if(str.trim().equalsIgnoreCase("887")) str="YEM";
	 if(str.trim().equalsIgnoreCase("894")) str="ZMB";
	 if(str.trim().equalsIgnoreCase("895")) str="ABH";
	 if(str.trim().equalsIgnoreCase("896")) str="OST";
	 if(str.trim().equalsIgnoreCase("0")) str="�/�";
	 if(str.trim().equalsIgnoreCase("422")) str="LBN";
	 if(str.trim().equalsIgnoreCase("426")) str="LSO";
	 if(str.trim().equalsIgnoreCase("428")) str="LVA";
	 if(str.trim().equalsIgnoreCase("430")) str="LBR";
	 if(str.trim().equalsIgnoreCase("434")) str="LBY";
	 if(str.trim().equalsIgnoreCase("438")) str="LIE";
	 if(str.trim().equalsIgnoreCase("440")) str="LTU";
	 if(str.trim().equalsIgnoreCase("442")) str="LUX";
	 if(str.trim().equalsIgnoreCase("446")) str="MAC";
	 if(str.trim().equalsIgnoreCase("450")) str="MDG";
	 if(str.trim().equalsIgnoreCase("454")) str="MWI";
	 if(str.trim().equalsIgnoreCase("458")) str="MYS";
	 if(str.trim().equalsIgnoreCase("462")) str="MDV";
	 if(str.trim().equalsIgnoreCase("466")) str="MLI";
	 if(str.trim().equalsIgnoreCase("470")) str="MLT";
	 if(str.trim().equalsIgnoreCase("474")) str="MTQ";
	 if(str.trim().equalsIgnoreCase("478")) str="MRT";
	 if(str.trim().equalsIgnoreCase("480")) str="MUS";
	 if(str.trim().equalsIgnoreCase("484")) str="MEX";
	 if(str.trim().equalsIgnoreCase("492")) str="MCO";
	 if(str.trim().equalsIgnoreCase("496")) str="MNG";
	 if(str.trim().equalsIgnoreCase("498")) str="MDA";
	 if(str.trim().equalsIgnoreCase("499")) str="MNE";
	 if(str.trim().equalsIgnoreCase("500")) str="MSR";
	 if(str.trim().equalsIgnoreCase("504")) str="MAR";
	 if(str.trim().equalsIgnoreCase("508")) str="MOZ";
	 if(str.trim().equalsIgnoreCase("512")) str="OMN";
	 if(str.trim().equalsIgnoreCase("516")) str="NAM";
	 if(str.trim().equalsIgnoreCase("520")) str="NRU";
	 if(str.trim().equalsIgnoreCase("524")) str="NPL";
	 if(str.trim().equalsIgnoreCase("528")) str="NLD";
	 if(str.trim().equalsIgnoreCase("531")) str="CUW";
	 if(str.trim().equalsIgnoreCase("533")) str="ABW";
	 if(str.trim().equalsIgnoreCase("534")) str="SXM";
	 if(str.trim().equalsIgnoreCase("535")) str="BES";
	 if(str.trim().equalsIgnoreCase("540")) str="NCL";
	 if(str.trim().equalsIgnoreCase("548")) str="VUT";
	 if(str.trim().equalsIgnoreCase("554")) str="NZL";
	 if(str.trim().equalsIgnoreCase("558")) str="NIC";
	 if(str.trim().equalsIgnoreCase("562")) str="NER";
	 if(str.trim().equalsIgnoreCase("566")) str="NGA";
	 if(str.trim().equalsIgnoreCase("570")) str="NIU";
	 if(str.trim().equalsIgnoreCase("574")) str="NFK";
	 if(str.trim().equalsIgnoreCase("578")) str="NOR";
	 if(str.trim().equalsIgnoreCase("580")) str="MNP";
	 if(str.trim().equalsIgnoreCase("581")) str="UMI";
	 if(str.trim().equalsIgnoreCase("583")) str="FSM";
	 if(str.trim().equalsIgnoreCase("584")) str="MHL";
	 if(str.trim().equalsIgnoreCase("585")) str="PLW";
	 if(str.trim().equalsIgnoreCase("586")) str="PAK";
	 if(str.trim().equalsIgnoreCase("591")) str="PAN";
	 if(str.trim().equalsIgnoreCase("598")) str="PNG";
	 if(str.trim().equalsIgnoreCase("600")) str="PRY";
	 if(str.trim().equalsIgnoreCase("604")) str="PER";
	 if(str.trim().equalsIgnoreCase("608")) str="PHL";
	 if(str.trim().equalsIgnoreCase("612")) str="PCN";
	 if(str.trim().equalsIgnoreCase("616")) str="POL";
	 if(str.trim().equalsIgnoreCase("620")) str="PRT";
	 if(str.trim().equalsIgnoreCase("624")) str="GNB";
	 if(str.trim().equalsIgnoreCase("626")) str="TLS";
	 if(str.trim().equalsIgnoreCase("630")) str="PRI";
	 if(str.trim().equalsIgnoreCase("634")) str="QAT";
	 if(str.trim().equalsIgnoreCase("638")) str="REU";
	 if(str.trim().equalsIgnoreCase("642")) str="ROU";
	 if(str.trim().equalsIgnoreCase("643")) str="RUS";
	 if(str.trim().equalsIgnoreCase("646")) str="RWA";
	 if(str.trim().equalsIgnoreCase("652")) str="BLM";
	 if(str.trim().equalsIgnoreCase("654")) str="SHN";
	 if(str.trim().equalsIgnoreCase("659")) str="KNA";
	 if(str.trim().equalsIgnoreCase("660")) str="AIA";
	 if(str.trim().equalsIgnoreCase("662")) str="LCA";
	 if(str.trim().equalsIgnoreCase("663")) str="MAF";
	 if(str.trim().equalsIgnoreCase("666")) str="SPM";
	 if(str.trim().equalsIgnoreCase("670")) str="VCT";
	 if(str.trim().equalsIgnoreCase("674")) str="SMR";
	 if(str.trim().equalsIgnoreCase("678")) str="STP";
	 if(str.trim().equalsIgnoreCase("682")) str="SAU";
	 if(str.trim().equalsIgnoreCase("686")) str="SEN";
	 if(str.trim().equalsIgnoreCase("688")) str="SRB";
	 if(str.trim().equalsIgnoreCase("690")) str="SYC";
	 if(str.trim().equalsIgnoreCase("694")) str="SLE";
	 if(str.trim().equalsIgnoreCase("702")) str="SGP";
	 if(str.trim().equalsIgnoreCase("703")) str="SVK";
	 if(str.trim().equalsIgnoreCase("704")) str="VNM";
	 if(str.trim().equalsIgnoreCase("705")) str="SVN";
	 if(str.trim().equalsIgnoreCase("706")) str="SOM";
	 if(str.trim().equalsIgnoreCase("710")) str="ZAF";
	 if(str.trim().equalsIgnoreCase("716")) str="ZWE";
	 if(str.trim().equalsIgnoreCase("724")) str="ESP";
	 if(str.trim().equalsIgnoreCase("728")) str="SSD";
	 if(str.trim().equalsIgnoreCase("729")) str="SDN";
	 if(str.trim().equalsIgnoreCase("732")) str="ESH";
	 if(str.trim().equalsIgnoreCase("740")) str="SUR";
	 if(str.trim().equalsIgnoreCase("744")) str="SJM";
	 if(str.trim().equalsIgnoreCase("748")) str="SWZ";
	 if(str.trim().equalsIgnoreCase("752")) str="SWE";
	 if(str.trim().equalsIgnoreCase("756")) str="CHE";
	 if(str.trim().equalsIgnoreCase("760")) str="SYR";
	 if(str.trim().equalsIgnoreCase("762")) str="TJK";
	 if(str.trim().equalsIgnoreCase("764")) str="THA";
	 if(str.trim().equalsIgnoreCase("768")) str="TGO";
	 if(str.trim().equalsIgnoreCase("772")) str="TKL";
	 if(str.trim().equalsIgnoreCase("776")) str="TON";
	 if(str.trim().equalsIgnoreCase("780")) str="TTO";
	 if(str.trim().equalsIgnoreCase("784")) str="ARE";
	 if(str.trim().equalsIgnoreCase("788")) str="TUN";
	 if(str.trim().equalsIgnoreCase("792")) str="TUR";
	 if(str.trim().equalsIgnoreCase("795")) str="TKM";
	 if(str.trim().equalsIgnoreCase("796")) str="TCA";
	 if(str.trim().equalsIgnoreCase("798")) str="TUV";
	 if(str.trim().equalsIgnoreCase("800")) str="UGA";
	 if(str.trim().equalsIgnoreCase("804")) str="UKR";
	 if(str.trim().equalsIgnoreCase("807")) str="MKD";
	 if(str.trim().equalsIgnoreCase("807")) str="MKD";

	 return str;
 }
 
 
 private ArrayList<ArrayList<String>> sonar(ArrayList<ArrayList<String>> ls)
 {
	 ArrayList<ArrayList<String>> info = new ArrayList<ArrayList<String>>();
	 int sht = 0;
     for (int i = 0; i < ls.size(); i++)
     {
    	 //System.out.println(ls.get(i).get(7)+"  "+ls.get(i).get(3)+"  "+ls.get(i).get(4)+"  "+ls.get(i).get(5)+"  "+ls.get(i).get(6)+"  "+ls.get(i).get(13)+"  "+ls.get(i).get(14)+"  "+ls.get(i).get(15)+" "+ls.get(i).get(16)+"  "+ls.get(i).get(17)+"  "+ls.get(i).get(18));
			 if(	(ls.get(i).get(7).equals("")||ls.get(i).get(3).equals("")||ls.get(i).get(4).equals("")||ls.get(i).get(5).equals("")||ls.get(i).get(6).equals("")||ls.get(i).get(13).equals("")||ls.get(i).get(14).equals("")||ls.get(i).get(15).equals("")||ls.get(i).get(16).equals("")||ls.get(i).get(17).equals("")||ls.get(i).get(18).equals("")		)	 && !ls.get(i).get(0).contains("A08") )
			 {
				sht = sht +1;
			 }
     }
     
     ArrayList<String> list = new ArrayList<String>();
		 list.add(String.valueOf(sht));
		 info.add(list);
		 return info;
 }
 
 /*
  * ����� ��������� ����� � ������ ������ ������� ����� � ������ ������� ������ � web-�������
  */
 private void addHead(List listWeb1)
 {
	 ArrayList<String> f= (ArrayList<String>)listWeb1.get(0);
	 // ���� �������� � ����� ������ 13
		if(f.size()<=13)
		{	
				 f= (ArrayList<String>)listWeb1.get(0);
				 f.add("PassDOC");f.add("NumDOC");f.add("DateDOC");f.add("BORN");f.add("GOVER");f.add("CODEDOC");f.add("SEX");f.add("BIRTHDAY");f.add("");f.add("D2");
				 
				 
				 // ��������� "�����" � ������ ������
				 for (int i = 1; i < listWeb1.size(); i++)
				 {
					 f= (ArrayList<String>)listWeb1.get(i);
					 f.add("");f.add("");f.add("");f.add("");f.add("");f.add("");f.add("");f.add("");f.add("");f.add("");
				 }
		}		 
				 
 }
 
 
 
 private int fun(String p1, String p2,PreparedStatement stmt,ResultSet rs,Connection conn) throws SQLException
 {
	 String status = null;
	 
	// String queryInDB="select count(*) from person p left join personadd pa on (person_addressid= personadd_addressid)"
	// + " where p.person_linksmoestablishmentid > 0 and (p.enp = '"+p1+"' or p.enp = '"+p2+"' or pa.enp = '"+p1+"' or pa.enp = '"+p2+"')";
	
	 String queryInDB="select sum(cnt) cnt from"
	 		+ "("
	 		+ "select count(*) cnt from person p  where p.person_linksmoestablishmentid > 0 and (p.enp  = '"+p1+"' or p.enp = '"+p2+"' )"
	 		+ "union "
	 		+ "select count(*) from person p inner join personadd pa on (person_addressid= personadd_addressid) where p.person_linksmoestablishmentid > 0 "
	 		+ "and ( pa.enp = '"+p1+"' or pa.enp = '"+p2+"')"
	 		+ ")";
	 
	 stmt = conn.prepareStatement(queryInDB);
     rs = stmt.executeQuery();
    
     while (rs.next())
     {
    	 status = rs.getString(1);
     }
     stmt.close();
	 return Integer.valueOf(status);
 }
 
 
 private Date parseDate(String cunvertCurrentDate,String format) throws ParseException
 {
	  Date date = new Date();
	  DateFormat df = new SimpleDateFormat(format);
	 if(!cunvertCurrentDate.equalsIgnoreCase(""))
	 {
		
		 date = df.parse(cunvertCurrentDate);
	 }
	 else
	 {
		 date = df.parse("15.15.2020");
	 }
	    return date;
 }
 
 private String parseStringDate(String cunvertCurrentDate) throws ParseException
 {
	 cunvertCurrentDate = cunvertCurrentDate.substring(6,10)+"-"+cunvertCurrentDate.substring(3,5)+"-"+cunvertCurrentDate.substring(0,2);
	    return cunvertCurrentDate;
 }
 
 
 
/*
 * ����� ��������  [5450730828000030, null, null, null, null, null, null, null, null, null, null] 
 * �� ""
 */
 private void chancheNUL(ArrayList<String> f)
 {
	 for (int i = 1; i < f.size(); i++)
	 {
		  f.set(i, "");
	 }
 }
	
 /*
  * ����� �������� � ������ �� zp1.
  * ���� ��� ����� ������ �������� 2 ������� ��� -> ��� ���(����������) �� ����� �������� �� �������� �02
  */
 private ArrayList<String> uniqueGUID(List listWeb3)
 {
	 ArrayList<String> st  = new ArrayList<String>();
	 
	 for (int i2 = 0; i2 < listWeb3.size()-1; i2++)
	 {
		 ArrayList<String> f= (ArrayList<String>)listWeb3.get(i2);
		 ArrayList<String> f_plus_1= (ArrayList<String>)listWeb3.get(i2+1);
		 
		 if(	f.get(5).trim().equals(f_plus_1.get(5).trim())	)
		 {
			 if(	!f.get(6).trim().equalsIgnoreCase(f_plus_1.get(6).trim())	)
			 {
				 st.add(f.get(0).trim());
			 }
		 }
	 }
	 
	 return st;
 }
 
}
