package com.winListStruct;

public class winListStruct {
	public String username; //�û���
	public String ip;//ip��ַ
	public String time;//ʱ��
	public int scores;//�÷�
	@Override
	public String toString() {
		return ""+username+" "+ip+" "+time+" "+scores; //��д�÷�������д��
	}
}
