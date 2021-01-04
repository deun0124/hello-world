package com.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.SettingDAO;
import com.model.SettingDTO;
import com.model.WaterDAO;
import com.model.WaterDTO;

@WebServlet("/wifi")
public class ArduinoFunctionWifi extends HttpServlet {

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		WaterDAO wdao = new WaterDAO();
		WaterDTO wdto = new WaterDTO();
		SettingDAO sdao = new SettingDAO();
		SettingDTO sdto = new SettingDTO();
		
		// input�� ---
		String serialCode = request.getParameter("sc");
		int waterLevel = Integer.parseInt(request.getParameter("wl"));
		String waterTemp = request.getParameter("tp");
		String feedPressure = request.getParameter("fp");  //  �����ļ����� ����
		// ----------
		
		System.out.println("���� input : " + waterLevel);
		
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		System.out.println(hour + "�� " + min + "�� " + sec + "��");
		
		// ���� cm�� ��ȯ ---
		int watercm = 12;
		System.out.println(watercm);
		if(waterLevel > 520) {
			watercm += 3;
		}else if(waterLevel > 430) {
			watercm += 2;
		}else if(waterLevel > 300) {
			watercm += 1;
		}
		String waterstrcm = Integer.toString(watercm);
		// ----------
		
		// db���� ---
		wdto.setWatertemp(waterTemp);
		wdto.setWaterlevel(waterstrcm);
		wdto.setFeedpressure(feedPressure);
		wdao.updateWater(wdto, serialCode);
		
		// ----------

		// �ڵ� üũ�� ---
		System.out.println("���� : " + waterTemp + "��");
		System.out.println("���� : " + watercm + "cm");
		System.out.println("���̷� : " + feedPressure);  // �������� ���� ������
		// ----------
		// output�� ---  (�Ƶ��̳�� ���� ����)
		String sv = "0";  // ��������
		String heat = "0";  // �߿��е�
		String ledInfo = "00";  // 00 : off, 01~13 : �÷�
		// ----------
		
		sdto = sdao.getSettingInfo(serialCode);
		
		// led���
		if(sdto.getSet_power().equals("off")) {
			ledInfo = "00";
		}else {
			ledInfo = sdto.getSet_color();
		}
		
		// ���̰���
		int t_t = Integer.parseInt(sdto.getSet_feed());  // web���� ������ ���޽ð�����
		
		String feeding = wdao.getWaterInfo(serialCode).getFeeding();
		
		for(int i = 0; i < 20; i++) {  // ���� �д� ����
			if(((t_t*i == min) && (wdao.updateFeed(t_t/2, serialCode) > 0)) || feeding.equals("ON")) {
				wdao.feedUpdateNow(serialCode);
				wdao.updateFeeding(serialCode, "OFF");
				sv = "1";
				break;
			}else {
				sv = "0";
			}
		}
		System.out.println((sv.equals("1"))? "�����ֱ�on" : "�����ֱ�off");
		
		// �߿��е�
		int set_Temp = Integer.parseInt(sdto.getSet_temp());
		float wTemp = Float.parseFloat(waterTemp);
		System.out.println("�µ����� : " + (float)(set_Temp-wTemp));
		if(set_Temp > wTemp) {
			heat = "1"; // 2. �߿��е�
			System.out.println("�߿��е� on");
		}else {
			heat = "0";
			System.out.println("�߿��е� off");
		}
		
		// /���̰���(1),led����(1),led��(2)/
		out.print("/" + sv + heat + ledInfo + "/");
	}

}
