package com.vijet.ir.robot;

public class RobotUtil {
	private static CustomRobot customRobot = new CustomRobot();
	
	public final static boolean canURLBeCrawled(final String url){
		return customRobot.checkUrl(url);
	}
}
