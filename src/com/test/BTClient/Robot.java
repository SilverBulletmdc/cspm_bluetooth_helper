package com.test.BTClient;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import com.test.BTClient.DeviceListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
//import android.view.Menu;            //��ʹ�ò˵����������
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Robot extends Activity implements Runnable { 
	
	private final static int REQUEST_CONNECT_DEVICE = 1;    //�궨���ѯ�豸���
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP����UUID��
	
	private InputStream is;    //������������������������

    private String smsg = "";    //��ʾ�����ݻ���
    private String fmsg = "";    //���������ݻ���
    boolean sym=false;
    
   
    

    public String filename=""; //��������洢���ļ���
    BluetoothDevice _device = null;     //�����豸
    BluetoothSocket _socket = null;      //����ͨ��socket
    boolean _discoveryFinished = false;    
    boolean bRun = true;
    boolean bThread = false;
	
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //��ȡ�����������������������豸
    private int port_int = 0;
    private String host = "";
	
    private TextView tv_msg = null;
    private EditText ed_msg = null;
    private Button btn_send = null;
    private Button connect_net_btn = null;
    private Button disconnect_net_btn = null;
    private Button send_to_pc_btn = null;
    
    // private Button btn_login = null;
    private static final String HOST = "172.31.176.158";
    private static final int PORT = 30000;
    private Socket socket = null;
    
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    //�����̷߳��͹�����Ϣ������TextView��ʾ
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            Log.d("handler","����handler");
            //CharSequence cs = content;
            tv_msg.setText(content);
            //content=content.substring(0,1);
            sendMessagebyBT(content);
            //out.println("�ѽ�����͸�С��");
        }
    };
    public Handler btn_Handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            connect_net_btn.setEnabled(false);
            disconnect_net_btn.setEnabled(true);
        }
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   //���û���Ϊ������ main.xml
        
        //text0 = (TextView)findViewById(R.id.Text0);  //�õ���ʾ�����
//        edit0 = (EditText)findViewById(R.id.Edit0);   //�õ��������
//        sv = (ScrollView)findViewById(R.id.ScrollView01);  //�õ���ҳ���
//        dis = (TextView) findViewById(R.id.in);      //�õ�������ʾ���

       //����򿪱��������豸���ɹ�����ʾ��Ϣ����������
        tv_msg = (TextView) findViewById(R.id.receive);
        ed_msg = (EditText) findViewById(R.id.sendtext);
        btn_send = (Button) findViewById(R.id.sendbutton);
        connect_net_btn = (Button)findViewById(R.id.connect_net_btn);
        disconnect_net_btn = (Button)findViewById(R.id.disconnect_btn);
        send_to_pc_btn = (Button)findViewById(R.id.send_to_pc_btn);

        
        btn_send.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String msg = ed_msg.getText().toString();
                TextView num_tv = (TextView)findViewById(R.id.num);
                if(!num_tv.getText().toString().isEmpty())
                	{
	                	String num_s = num_tv.getText().toString();
	                	int num = Integer.parseInt(num_s);
	                	for(int i = 1; i <= num; i++)
	                		sendMessagebyBT(msg);
                	}
                sendMessagebyBT(msg);
                
                
                
            }
        });
        send_to_pc_btn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String msg = ed_msg.getText().toString();
                TextView num_tv = (TextView)findViewById(R.id.num);
                if(!num_tv.getText().toString().isEmpty())
                	{
	                	String num_s = num_tv.getText().toString();
	                	int num = Integer.parseInt(num_s);
	                	for(int i = 1; i <= num; i++)
	                		if(out != null)
	                		{
	                			out.println(msg);
	                			out.flush();
	                		}
	                		else
	                			Toast.makeText(getApplicationContext(), "����δ����", Toast.LENGTH_SHORT).show();
                	}
                else{
                		if(out != null)
                		{
                			out.println(msg);
                			out.flush();
                		}
                		else
                			Toast.makeText(getApplicationContext(), "����δ����", Toast.LENGTH_SHORT).show();
                	}
                
                
                
                
            }
        });
        connect_net_btn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				TextView ip_tv = (TextView)findViewById(R.id.ip);
				TextView port_tv = (TextView)findViewById(R.id.port);
				host = ip_tv.getText().toString();
				String port = port_tv.getText().toString();
				port_int = 0;
				if(!host.equals("") && !port.equals(""))
					port_int = Integer.parseInt(port);
				else
				{
					Toast.makeText(getApplicationContext(), "��������ȷ��IP�Ͷ˿ڵ�ַ", Toast.LENGTH_SHORT).show();
					return;
				}
				 new Thread(){
			    	   public void run(){
			    		   try {
								

					            socket = new Socket(host, port_int);
					            btn_Handler.sendMessage(mHandler.obtainMessage());
					            in = new BufferedReader(new InputStreamReader(socket
					                    .getInputStream()));
					            Log.d("mengdechao","in�ѳ�ʼ��");
					            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
					        } catch (IOException ex) {
					            ex.printStackTrace();
					           
					        }
			    		   Log.d("internet","����run����");
			    			//Toast.makeText(getApplicationContext(), "����ѭ��", Toast.LENGTH_SHORT).show();
			    			// TODO Auto-generated method stub
			    			  try {
			    		            while (true) {
			    		            	Log.d("internet","����ѭ��");
			    		            	if(socket != null){
			    		            		Log.d("internet","�����һ���ж�");
			    		            		
			    			                if (!socket.isClosed()) {
			    			                	Log.d("internet","����ڶ����ж�");
			    			                	
			    	                            
			    			                    if (socket.isConnected()) {
			    			                        if (!socket.isInputShutdown()) {
			    			                        	if((content = in.readLine()) != null)
			    			                        		Log.d("internet","����������ж�");
			    			                        		
			    			                        		Log.d("internet","readline��ִ��");
			    			                                mHandler.sendMessage(mHandler.obtainMessage());
			    			                                
			    			                                
			    			                           
			    			                        }
			    			                    }
			    			                }
			    			            else
			    			                break;
			    		            	}
			    		            }
			    		        } catch (Exception e) {
			    		            e.printStackTrace();
			    		        }
			    		   
			    	   }   	   
			       }.start();    
				
		
			}
        	
        });
        disconnect_net_btn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					socket.close();
					connect_net_btn.setEnabled(true);
					disconnect_net_btn.setEnabled(false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
        //�����̣߳����շ��������͹���������
        new Thread(Robot.this).start();
        Button upbutton=(Button)findViewById(R.id.upbutton);
        Button leftbutton=(Button)findViewById(R.id.leftbutton);
        Button rightbutton=(Button)findViewById(R.id.rightbutton);
        Button backbutton=(Button)findViewById(R.id.backbutton);
        SeekBar speed=(SeekBar)findViewById(R.id.seekBar1);
       
        OnSeekBarChangeListener osc=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				  TextView tv=(TextView)findViewById(R.id.textView1);
				  tv.setText((CharSequence)Integer.toString(arg0.getProgress()));
				  if(arg0.getProgress()!=102&&arg0.getProgress()!=108&&arg0.getProgress()!=114&&arg0.getProgress()!=98)
				  sendMessagebyBT(String.valueOf((char)arg0.getProgress()));
			}
		};
		 speed.setOnSeekBarChangeListener(osc);
		 OnTouchListener otl=new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				if(arg1.getAction()==MotionEvent.ACTION_DOWN){
					
					String s=null;
					if(arg0.getId()==R.id.upbutton)
						s="f";
				
					if(arg0.getId()==R.id.backbutton)
						s="b";
					if(arg0.getId()==R.id.leftbutton)
						s="l";
					if(arg0.getId()==R.id.rightbutton)
						s="r";
					sendMessagebyBT(s);
					
				}
				if(arg1.getAction()==MotionEvent.ACTION_UP){
					sendMessagebyBT("s");
				}
			
				return false;
			}
		};
    
        upbutton.setOnTouchListener(otl);
        leftbutton.setOnTouchListener(otl);
        backbutton.setOnTouchListener(otl);
        rightbutton.setOnTouchListener(otl);
        
        if (_bluetooth == null){
        	Toast.makeText(this, "�޷����ֻ���������ȷ���ֻ��Ƿ����������ܣ�", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // �����豸���Ա�����  
       new Thread(){
    	   public void run(){
    		   if(_bluetooth.isEnabled()==false){
        		_bluetooth.enable();
    		   }
    	   }   	   
       }.start();      
    }

    //���Ͱ�����Ӧ
//    public void onSendButtonClicked(View v){
//    	int i=0;
//    	int n=0;
//    	try{
//    		OutputStream os = _socket.getOutputStream();   //�������������
//    		byte[] bos = edit0.getText().toString().getBytes();
//    		for(i=0;i<bos.length;i++){
//    			if(bos[i]==0x0a)n++;
//    		}
//    		byte[] bos_new = new byte[bos.length+n];
//    		n=0;
//    		for(i=0;i<bos.length;i++){ //�ֻ��л���Ϊ0a,�����Ϊ0d 0a���ٷ���
//    			if(bos[i]==0x0a){
//    				bos_new[n]=0x0d;
//    				n++;
//    				bos_new[n]=0x0a;
//    			}else{
//    				bos_new[n]=bos[i];
//    			}
//    			n++;
//    		}
//    		
//    		os.write(bos_new);	
//    	}catch(IOException e){  		
//    	}  	
//    }
    
    //���ջ�������ӦstartActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:     //���ӽ������DeviceListActivity���÷���
    		// ��Ӧ���ؽ��
            if (resultCode == Activity.RESULT_OK) {   //���ӳɹ�����DeviceListActivity���÷���
                // MAC��ַ����DeviceListActivity���÷���
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // �õ������豸���      
                _device = _bluetooth.getRemoteDevice(address);
 
                // �÷���ŵõ�socket
                try{
                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                }catch(IOException e){
                	Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
                }
                //����socket
            	Button btn = (Button) findViewById(R.id.Button03);
                try{
                	_socket.connect();
                	Toast.makeText(this, "����"+_device.getName()+"�ɹ���", Toast.LENGTH_SHORT).show();
                	btn.setText("�Ͽ�");
                }catch(IOException e){
                	try{
                		Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
                		_socket.close();
                		_socket = null;
                	}catch(IOException ee){
                		Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
                	}
                	
                	return;
                }
                
                //�򿪽����߳�
                try{
            		is = _socket.getInputStream();   //�õ���������������
            		}catch(IOException e){
            			Toast.makeText(this, "��������ʧ�ܣ�", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if(bThread==false){
            			ReadThread.start();
            			bThread=true;
            		}else{
            			bRun = true;
            		}
            }
    		break;
    	default:break;
    	}
    }
    
    //���������߳�
    Thread ReadThread=new Thread(){
    	
    	public void run(){
    		int num = 0;
    		byte[] buffer = new byte[1024];
    		byte[] buffer_new = new byte[1024];
    		int i = 0;
    		int n = 0;
    		bRun = true;
    		//�����߳�
    		while(true){
    			try{
    				while(is.available()==0){
    					while(bRun == false){}
    				}
    				while(true){
    					num = is.read(buffer);         //��������
    					Log.d("internet","��Ϣ����");
    					n=0;
    					
    					String s0 = new String(buffer,0,num);
    					fmsg+=s0;    //�����յ�����
    					for(i=0;i<num;i++){
    						if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
    							buffer_new[n] = 0x0a;
    							i++;
    						}else{
    							buffer_new[n] = buffer[i];
    						}
    						n++;
    					}
    					String s = new String(buffer_new,0,n);
    					smsg=s;   //д����ջ���
    					if(is.available()==0)break;  //��ʱ��û�����ݲ�����������ʾ
    				}
    				//������ʾ��Ϣ��������ʾˢ��
    					handler.sendMessage(handler.obtainMessage());       	    		
    	    		}catch(IOException e){
    	    		}
    		}
    	}
    };
    
    //��Ϣ�������
    Handler handler= new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		TextView in=(TextView)findViewById(R.id.in);
    		in.setText(smsg);
    		//dis.setText(smsg);   //��ʾ���� 
    		//sv.scrollTo(0,dis.getMeasuredHeight()); //�����������һҳ
    	}
    };
    
    //�رճ�����ô�����
    public void onDestroy(){
    	super.onDestroy();
    	if(_socket!=null)  //�ر�����socket
    	try{
    		_socket.close();
    	}catch(IOException e){}
    //	_bluetooth.disable();  //�ر���������
    }
    
 
    
    //���Ӱ�����Ӧ����
    public void onConnectButtonClicked(View v){ 
    	if(_bluetooth.isEnabled()==false){  //����������񲻿�������ʾ
    		Toast.makeText(this, " ��������...", Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	
        //��δ�����豸���DeviceListActivity�����豸����
    	Button btn = (Button) findViewById(R.id.Button03);
    	if(_socket==null){
    		Intent serverIntent = new Intent(this, DeviceListActivity.class); //��ת��������
    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //���÷��غ궨��
    	}
    	else{
    		 //�ر�����socket
    	    try{
    	    	
    	    	is.close();
    	    	_socket.close();
    	    	_socket = null;
    	    	bRun = false;
    	    	btn.setText("����");
    	    }catch(IOException e){}   
    	}
    	return;
    }
    
    //���水����Ӧ����
//    public void onSaveButtonClicked(View v){
//    	Save();
//    }
//    
//    //���������Ӧ����
//    public void onClearButtonClicked(View v){
//    	smsg="";
//    	fmsg="";
//    	dis.setText(smsg);
//    	return;
//    }
//    
    //�˳�������Ӧ����
    public void onQuitButtonClicked(View v){
    	finish();
    }
    
    //���湦��ʵ��
//	private void Save() {
//		//��ʾ�Ի��������ļ���
//		LayoutInflater factory = LayoutInflater.from(BTClient.this);  //ͼ��ģ�����������
//		final View DialogView =  factory.inflate(R.layout.sname, null);  //��sname.xmlģ��������ͼģ��
//		new AlertDialog.Builder(BTClient.this)
//								.setTitle("�ļ���")
//								.setView(DialogView)   //������ͼģ��
//								.setPositiveButton("ȷ��",
//								new DialogInterface.OnClickListener() //ȷ��������Ӧ����
//								{
//									public void onClick(DialogInterface dialog, int whichButton){
//										EditText text1 = (EditText)DialogView.findViewById(R.id.sname);  //�õ��ļ����������
//										filename = text1.getText().toString();  //�õ��ļ���
//										
//										try{
//											if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //���SD����׼����
//												
//												filename =filename+".txt";   //���ļ���ĩβ����.txt										
//												File sdCardDir = Environment.getExternalStorageDirectory();  //�õ�SD����Ŀ¼
//												File BuildDir = new File(sdCardDir, "/data");   //��dataĿ¼���粻����������
//												if(BuildDir.exists()==false)BuildDir.mkdirs();
//												File saveFile =new File(BuildDir, filename);  //�½��ļ���������Ѵ������½��ĵ�
//												FileOutputStream stream = new FileOutputStream(saveFile);  //���ļ�������
//												stream.write(fmsg.getBytes());
//												stream.close();
//												Toast.makeText(BTClient.this, "�洢�ɹ���", Toast.LENGTH_SHORT).show();
//											}else{
//												Toast.makeText(BTClient.this, "û�д洢����", Toast.LENGTH_LONG).show();
//											}
//										
//										}catch(IOException e){
//											return;
//										}
//										
//										
//										
//									}
//								})
//								.setNegativeButton("ȡ��",   //ȡ��������Ӧ����,ֱ���˳��Ի������κδ��� 
//								new DialogInterface.OnClickListener() {
//									public void onClick(DialogInterface dialog, int which) { 
//									}
//								}).show();  //��ʾ�Ի���
//	} 
//
 public void sendMessagebyBT(String s){
	 if(_socket != null){
		 if (_socket.isConnected()) {
			 int i=0;
		    	int n=0;
		    	try{
		    		OutputStream os = _socket.getOutputStream();   //�������������
		    		byte[] bos = s.getBytes();
		    		for(i=0;i<bos.length;i++){
		    			if(bos[i]==0x0a)n++;
		    		}
		    		byte[] bos_new = new byte[bos.length+n];
		    		n=0;
		    		for(i=0;i<bos.length;i++){ //�ֻ��л���Ϊ0a,�����Ϊ0d 0a���ٷ���
		    			if(bos[i]==0x0a){
		    				bos_new[n]=0x0d;
		    				n++;
		    				bos_new[n]=0x0a;
		    			}else{
		    				bos_new[n]=bos[i];
		    			}
		    			n++;
		    		}
		    		
		    		os.write(bos_new);	
		    	}catch(IOException e){  		
		    	}  	
	     }
		 else
			 Toast.makeText(getApplicationContext(), "����δ����", Toast.LENGTH_SHORT).show();
	
	 }
	 else
		 Toast.makeText(getApplicationContext(), "����δ����", Toast.LENGTH_SHORT).show();
    
		
    	
	
    	
    }

	@Override
	public void run() {
		
	}
    

    
public void ShowDialog(String msg) {
    new AlertDialog.Builder(this).setTitle("notification").setMessage(msg)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
}
}