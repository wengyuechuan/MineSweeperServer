package com.serverFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 * ��������
 */
import com.winListStruct.winListStruct;
public class serverFrame extends JFrame implements Runnable, ListSelectionListener, ActionListener{
	private Socket s=null;
	private ServerSocket ss=null;
    private ArrayList<gameThread> users = new ArrayList<gameThread>(); //�����ܹ���̬���������� �洢��Щ�û��߳�
	private HashMap<String,String> userMsg=new HashMap<>(); //���ڴ洢�û��˺ź�����Ķ�Ӧ��ϵ
	DefaultListModel<String> nowusername = new DefaultListModel<String>(); //���ڴ洢��ǰ�û�������
	private JList<String> userList=new JList(nowusername);//��ʾ�����б����������û�ѡ��һ�����߶��������
	private ArrayList<winListStruct> winlist=new ArrayList<>();//�µĽṹ��
	private JButton jbt=new JButton("�߳�������");
	public serverFrame() throws Exception{
		this.setTitle("ɨ�׷�����");
		this.add(userList,BorderLayout.NORTH);
		this.add(jbt,BorderLayout.SOUTH);
		jbt.addActionListener(this);//ʵ������
		this.setDefaultCloseOperation(EXIT_ON_CLOSE); //Ĭ�Ͽ��Թر�
		this.setLocation(400,400);
		this.setSize(500,400);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		ss=new ServerSocket(9999);//�˿�9999
		new Thread(this).start();//�������߳̽��м���
	}
	@Override
	public void actionPerformed(ActionEvent e) { //�߳������������
		// TODO Auto-generated method stub
		String label=e.getActionCommand();
		if(label.equals("�߳�������")) {
			try {
				handleExpel();
			}catch(IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		while(true) {
			try {
				s=ss.accept();
				gameThread ct=new gameThread(s);
				users.add(ct);//���̼߳��뵽����
				ListModel<String> model=userList.getModel();//��ȡJList����������
				ct.start();//�����߳�
			}catch(Exception ex) {
				ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,"�������쳣��");
                System.exit(0);
			}
		}
		
	}
	
	public void readFromFile() throws IOException { //���ļ��ж�������
		File file=new File("./userMsg/user.txt"); //���ļ�
        BufferedReader reader = null;
		try {
			reader=new BufferedReader(new FileReader(file));//�����ļ�
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String temp=null;
		while((temp=reader.readLine())!=null) {
			String[] temp2=temp.split(" "); 
			this.userMsg.put(temp2[0], temp2[1]);//����������Ԫ��
		}
		reader.close();//�ر���
	}
	
	public void writeToFile() throws IOException{ //���ļ���д������
		File file=new File("./userMsg/user.txt");//���ļ�
		BufferedWriter writer=null;
		writer=new BufferedWriter(new FileWriter(file));//���ļ��ַ������
		for(String k:userMsg.keySet()) {
			writer.write(k+" "+userMsg.get(k)); //�õ���ֵ��
			writer.write('\n'); //д��һ�����з�
		}
		writer.flush();//д�뵽�ļ���
		writer.close();//�ر��ļ������
	}
	
	public void getWin(String str) throws IOException{ //�õ����а��е�ǰ���� ���Խ�����Ľṹ�ĳ�һ������������д
		File file=new File("./userMsg/winList.txt");
		BufferedReader reader=new BufferedReader(new FileReader(file));//���ļ�
		String temp=null;
		while((temp=reader.readLine())!=null) { //����
			String[] temp2=temp.split(" ");
			winListStruct temp3 = new winListStruct();
			temp3.username=temp2[0];
			temp3.ip=temp2[1];
			temp3.time=temp2[2];
			temp3.scores=Integer.parseInt(temp2[3]);
			winlist.add(temp3);
		}
		Collections.sort(winlist,new Comparator<winListStruct>() {

			@Override
			public int compare(winListStruct o1, winListStruct o2) {
				return o2.scores-o1.scores;//��������
			}
		});
		for(int i=0;i<3;i++) {
			str+=(winlist.get(i).toString()+"#");//�õ�ǰ�����ĳɼ� ��#�ָ����еķ��ص���ֵ
		}
		reader.close();
	}
	
	public void putWin(winListStruct nowu) throws IOException{ //д�����а� δʵ�֣� ��û��������������ݽṹ
		File file=new File("./userMsg/winList.txt");
		BufferedWriter writer=new BufferedWriter(new FileWriter(file));//���ļ�
		String content="";
		winlist.add(nowu);
		for(winListStruct wls:winlist) {
			content+=wls.toString();
			content+="\n";//����һ�п���
		}
	}
	
	public class gameThread extends Thread{ //��Ϸ�̣߳��������ɶ��û���������Ϣ����
		Socket s=null;
		public boolean canRun=true;
		private BufferedReader br=null;//���뻺����
		private PrintStream ps=null;//���������
		private String username=null; //�û���
		public gameThread(Socket s) throws Exception{
			this.s=s;
			br=new BufferedReader(new InputStreamReader(s.getInputStream()));//��������뻺������ʼ��
			ps=new PrintStream(s.getOutputStream());//�õ����
		}
		public void run() {
			while(canRun) {
				try {
                    String msg = br.readLine();//���տͻ��˷�������Ϣ
                    String[] strs = msg.split("#");
                    if(strs[0].equals("LOGIN")){//�յ����Կͻ��˵�������Ϣ
                        this.username = strs[1]; //��ȡ�û���
                        userMsg.clear();//��ձ��Ȼ����ļ������»�ȡ
                        readFromFile();//���ļ��ж������¸��±�
                        String pwd=userMsg.get(this.username);//�õ��û�����Ӧ������
                        if(pwd.equals(strs[2])) {//����õ�������Ҳ������ͬ�������������¼
                            nowusername.addElement(username);
                            userList.repaint();
                            sendMessage("ACCEPTLOGIN#",this);
                        }else {
                        	sendMessage("REFUSELOGIN#",this);//�ܾ���¼
                        }
                    }else if(strs[0].equals("REGISTER")){ //�����ע��
                        ArrayList<String> usem=(ArrayList<String>) userMsg.keySet();
                        String newuname=strs[1];//��ȡע����û���
                        if(usem.contains(newuname)) {
                        	sendMessage("REFUSEREGISTER#",this);
                        }else {
                        	userMsg.put(newuname, strs[3]);//������Ҳ��ӽ�ȥ
                        	writeToFile();//����д���ļ�
                        	sendMessage("ACCEPTREGISTER#",this);                      	
                        }
                    }else if(strs[0].equals("PUTWIN")){//�յ��ͻ���д�����а�
                    	winListStruct t=new winListStruct();
                    	t.username=strs[1];
                    	t.ip=strs[2];
                    	t.time=strs[3];
                    	t.scores=Integer.parseInt(strs[4]);//�������ת��
                    	putWin(t);       	
                    }else if(strs[0].equals("GETWIN")) {//�յ��ͻ����������а�
                    	String temp="GETWINRESULT#";
                    	getWin(temp);
                    	sendMessage(temp,this);
                    }
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
    public void handleExpel() throws IOException {
    	for(gameThread t:users) {
    		if(userList.getSelectedValuesList().get(0).equals(t.username)) {
                sendMessage("OFFLINE#" + userList.getSelectedValuesList().get(0),t); //��ǰ�̷߳���
    		}
    	}
        nowusername.removeElement(userList.getSelectedValuesList().get(0));//����defaultModel
        userList.repaint();//����Jlist
    }//����
	public void sendMessage(String msg,gameThread gt) {
		gt.ps.println(msg); //����Ϣ���ݻ�ȥ
	}
	public static void main(String[] args) throws Exception{
		new serverFrame();
	}
	
}
