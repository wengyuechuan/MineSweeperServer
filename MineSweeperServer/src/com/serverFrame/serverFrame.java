package com.serverFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 服务器端
 */
import com.winListStruct.winListStruct;
public class serverFrame extends JFrame implements Runnable, ListSelectionListener, ActionListener{
	private static final long serialVersionUID = 7927620805824794276L;
	private Socket s=null;
	private ServerSocket ss=null;
    private ArrayList<gameThread> users = new ArrayList<gameThread>(); //容量能够动态增长的数组 存储这些用户线程
	private HashMap<String,String> userMsg=new HashMap<>(); //用于存储用户账号和密码的对应关系
	DefaultListModel<String> nowusername = new DefaultListModel<String>(); //用于存储当前用户的名称
	private JList<String> userList=new JList<>(nowusername);//显示对象列表，并且允许用户选择一个或者多个项的组件
	private ArrayList<winListStruct> winlist=new ArrayList<>();//新的结构替
	private JButton jbt=new JButton("踢出服务器");
	public serverFrame() throws Exception{
		this.setTitle("扫雷服务器");
		this.add(userList,BorderLayout.NORTH);
		this.add(jbt,BorderLayout.SOUTH);
		jbt.addActionListener(this);//实现踢人
		this.setDefaultCloseOperation(EXIT_ON_CLOSE); //默认可以关闭
		this.setLocation(400,400);
		this.setSize(500,400);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		ss=new ServerSocket(9999);//端口9999
		new Thread(this).start();//启动主线程进行监听
	}
	@Override
	public void actionPerformed(ActionEvent e) { //踢出服务器的设计
		
		String label=e.getActionCommand();
		if(label.equals("踢出服务器")) {
			try {
				handleExpel();
			}catch(IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		
	}

	@Override
	public void run() {
		while(true) {
			try {
				s=ss.accept();
				gameThread ct=new gameThread(s);
				users.add(ct);//将线程加入到其中
				ct.start();//开启线程
			}catch(Exception ex) {
				ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,"服务器异常！");
                System.exit(0);
			}
		}
		
	}
	
	public void readFromFile() throws IOException { //从文件中读入数据
		userMsg.clear();//清空之后重新读入
		File file=new File("./userMsg/user.txt"); //打开文件
        BufferedReader reader = null;
		try {
			reader=new BufferedReader(new FileReader(file));//读入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String temp=null;
		while((temp=reader.readLine())!=null) {
			String[] temp2=temp.split(" "); 
			this.userMsg.put(temp2[0], temp2[1]);//向其中增加元素
		}
		reader.close();//关闭流
	}
	
	public void writeToFile() throws IOException{ //向文件中写入数据
		File file=new File("./userMsg/user.txt");//打开文件       
		BufferedWriter writer=null;
		writer=new BufferedWriter(new FileWriter(file));//打开文件字符输出流
		for(String k:userMsg.keySet()) {
			writer.write(k+" "+userMsg.get(k)); //得到键值对
			writer.write('\n'); //写入一个换行符
		}
		writer.flush();//写入到文件中
		writer.close();//关闭文件输出流
	}
	
	public String getWin(String str) throws IOException{ //得到排行榜中的前三名 可以将这里的结构改成一个类对象进行书写
		File file=new File("./userMsg/winList.txt");
		BufferedReader reader=new BufferedReader(new FileReader(file));//打开文件
		String temp=null;
		winlist.clear();//先清空所有的值
		while((temp=reader.readLine())!=null) { //读入
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
				return o2.scores-o1.scores;//降序排列
			}
		});
		int count = 3 < winlist.size()?3:winlist.size();
		for(int i=0;i<count;i++) {
			str+=(winlist.get(i).toString()+"#");//得到前三名的成绩 用#分割所有的返回的数值
		}
		reader.close();
		return str;
	}
	
	public void putWin(winListStruct nowu) throws IOException{ 
		File file=new File("./userMsg/winList.txt");
		BufferedReader reader=new BufferedReader(new FileReader(file));//打开文件输入流，先重新写入ArrayList
		winlist.clear();//清空这里的数值
		String temp=null;
		while((temp=reader.readLine())!=null) { 
			String[] temp2=temp.split(" ");
			winListStruct temp3 = new winListStruct();
			temp3.username=temp2[0];
			temp3.ip=temp2[1];
			temp3.time=temp2[2];
			temp3.scores=Integer.parseInt(temp2[3]);
			winlist.add(temp3);
		}
		reader.close();
		String content="";
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			winlist.add(nowu);
			for(winListStruct wls:winlist) {
				content+=wls.ToString(); //自己写的函数，专门用于描述这个形式
				content+="\n";//加入一行空行
			}
			writer.write(content);
			writer.flush();
		}
	}
	
	public class gameThread extends Thread{ //游戏线程，用于容纳多用户，用于信息传输
		Socket s=null;
		
		private BufferedReader br=null;//读入缓冲区
		private PrintStream ps=null;//输出缓冲区
		private String username=null; //用户名
		public gameThread(Socket s) throws Exception{
			this.s=s;
			br=new BufferedReader(new InputStreamReader(s.getInputStream()));//这里给输入缓冲区初始化
			ps=new PrintStream(s.getOutputStream());//得到输出
		}
		public void run() {
			while(true) {
				
				try {
					String msg = "";
					try {
						 msg = br.readLine();//接收客户端发来的消息
					}catch(Exception e) {
						System.out.println(Thread.currentThread().getName()+"客户端已经退出");
						break;
					}
                    String[] strs = msg.split("#");
                    if(strs[0].equals("LOGIN")){//收到来自客户端的上线消息
                        this.username = strs[1]; //获取用户名
                        userMsg.clear();//清空表项，然后从文件中重新获取
                        readFromFile();//从文件中读入重新更新表
                        String pwd=userMsg.get(this.username);//得到用户名对应的密码
                        if(pwd!=null && pwd.equals(strs[2])) {//如果得到的密码也与其相同，则现在允许登录
                            nowusername.addElement(username);
                            userList.repaint();
                            sendMessage("ACCEPTLOGIN#",this);
                            Thread.currentThread().setName(username);
                        }else {
                        	sendMessage("REFUSELOGIN#",this);//拒绝登录
                        }
                    }else if(strs[0].equals("REGISTER")){ //如果是注册
                        String newuname=strs[1];//获取注册的用户名
                        readFromFile();
                        if(userMsg.keySet().contains(newuname)) {//用户名已存在
                        	sendMessage("REFUSEREGISTER#用户名已存在",this);
                        }else if(strs[2].equals(strs[3]) == false){//两次密码不相同
                        	sendMessage("REFUSEREGISTER#两次密码不相同",this);
                        }else {
                        	userMsg.put(newuname, strs[3]);//把密码也添加进去
                        	writeToFile();//重新写回文件
                        	sendMessage("ACCEPTREGISTER#",this);                      	
                        }
                    }else if(strs[0].equals("PUTWIN")){//收到客户端写入排行榜
                    	winListStruct t=new winListStruct();
                    	t.username=strs[1];
                    	t.ip=strs[2];
                    	t.time=strs[3];
                    	t.scores=Integer.parseInt(strs[4]);//将其进行转换
                    	putWin(t);       	
                    }else if(strs[0].equals("GETWIN")) {//收到客户端请求排行榜
                    	String temp="GETWINRESULT#";
                    	temp=getWin(temp);
                    	sendMessage(temp,this);
                    }else if(strs[0].equals("OFFLINE")) {
                    	String echo = msg;//将收到的信息发送回客户端
                    	sendMessage(echo,this);//把OFFLINE请求发送回客户端，让客户端退出
                    	if(strs.length > 1)
                    		nowusername.removeElement(strs[1]);//删除客户端
                		userList.repaint();
                		
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
                sendMessage("OFFLINE#" + userList.getSelectedValuesList().get(0)+ "#" +"KICK",t); //向当前线程发送
    		}
    	}
        nowusername.removeElement(userList.getSelectedValuesList().get(0));//更新defaultModel
        userList.repaint();//更新Jlist
    }//踢人
	public void sendMessage(String msg,gameThread gt) {
		gt.ps.println(msg); //将信息传递回去
	}
	public static void main(String[] args) throws Exception{
		new serverFrame();
	}
	
}
