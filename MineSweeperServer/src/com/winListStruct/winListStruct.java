package com.winListStruct;

public class winListStruct {
	public String username; //用户名
	public String ip;//ip地址
	public String time;//时间
	public int scores;//得分
	@Override
	public String toString() {
		return ""+username+" "+ip+" "+time+" "+scores; //重写该方法方便写入
	}
}
