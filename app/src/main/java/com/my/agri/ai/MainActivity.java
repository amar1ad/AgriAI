package com.my.agri.ai;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.resources.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.graphics.*;
import android.util.Base64;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;


public class MainActivity extends AppCompatActivity {
	
	public final int REQ_CD_FP = 101;
	
	private ArrayList<HashMap<String, Object>> chatList = new ArrayList<>();
	private BaseAdapter adapter;
	private android.net.Uri selectedImageUri = null;
	private String API_KEY = "AIzaSyCEtpXNT5ax3H3E0a6c5wicpoieyA7TLAc";
	
	private LinearLayout header;
	private ListView listview_chat;
	private LinearLayout input_card;
	private ImageView imageview3;
	private LinearLayout linear2;
	private ImageView imageview4;
	private TextView textview3;
	private TextView textview4;
	private ImageButton button_image;
	private EditText edittext_msg;
	private ImageButton button_send;
	
	private Intent fp = new Intent(Intent.ACTION_GET_CONTENT);
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		header = findViewById(R.id.header);
		listview_chat = findViewById(R.id.listview_chat);
		input_card = findViewById(R.id.input_card);
		imageview3 = findViewById(R.id.imageview3);
		linear2 = findViewById(R.id.linear2);
		imageview4 = findViewById(R.id.imageview4);
		textview3 = findViewById(R.id.textview3);
		textview4 = findViewById(R.id.textview4);
		button_image = findViewById(R.id.button_image);
		edittext_msg = findViewById(R.id.edittext_msg);
		button_send = findViewById(R.id.button_send);
		fp.setType("image/*");
		fp.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		
		button_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, 100);
			}
		});
		
		button_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				String msg = edittext_msg.getText().toString().trim();
				if (!msg.isEmpty() || selectedImageUri != null) {
					addMessage(msg.isEmpty() ? "جاري تحليل الصورة..." : msg, true);
					startAnalysis(msg);
					edittext_msg.setText("");
				}
				
				
			}
		});
	}
	
	private void initializeLogic() {
		
		setupChatAdapter();
		
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		if (_requestCode == 100 && _resultCode == RESULT_OK && _data != null) {
			selectedImageUri = _data.getData();
			addMessage("🖼️ تم إرفاق الصورة. اضغط على زر الإرسال للبدء بالتحليل.", true);
		}
		switch (_requestCode) {
			case REQ_CD_FP:
			if (_resultCode == Activity.RESULT_OK) {
				ArrayList<String> _filePath = new ArrayList<>();
				if (_data != null) {
					if (_data.getClipData() != null) {
						for (int _index = 0; _index < _data.getClipData().getItemCount(); _index++) {
							ClipData.Item _item = _data.getClipData().getItemAt(_index);
							_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _item.getUri()));
						}
					}
					else {
						_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _data.getData()));
					}
				}
				
			}
			else {
				
			}
			break;
			default:
			break;
		}
	}
	
	public void _extra() {
		// نضع هذا الكود داخل بلوك add source directly في onCreate أو في More Block
		
	} // إغلاق دالة onCreate مؤقتاً لتعريف الدوال خارجها
	
	// ==========================================
	// 1. إعدادات السيرفر
	// ==========================================
	private String server_url = "http://0.0.0.0:8081/php/api.php";
	
	// ==========================================
	// 2. دوال إضافة الرسائل (تم حل مشكلة الـ 2 و 3 متغيرات)
	// ==========================================
	
	// دالة تقبل متغيرين (للتوافق مع الأكواد القديمة)
	private void addMessage(final String m, final boolean u) {
		addMessage(m, u, ""); 
	}
	
	// دالة تقبل 3 متغيرات (لدعم الصور من MySQL)
	private void addMessage(final String m, final boolean u, final String base64Image) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				HashMap<String, Object> map = new HashMap<>();
				map.put("text", m);
				map.put("isUser", String.valueOf(u));
				map.put("image", (base64Image == null) ? "" : base64Image);
				chatList.add(map);
				if (adapter != null) adapter.notifyDataSetChanged();
				listview_chat.setSelection(chatList.size() - 1);
			}
		});
	}
	
	// ==========================================
	// 3. دالة البحث في قاعدة بيانات MySQL المحلية
	// ==========================================
	private void searchDatabase(final String query) {
		// إظهار رسالة انتظار
		addMessage("جاري البحث في المكتبة الزراعية...", false, "");
		
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					java.net.URL url = new java.net.URL(server_url);
					java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					
					String postData = "action=search_db&query=" + java.net.URLEncoder.encode(query, "UTF-8");
					conn.getOutputStream().write(postData.getBytes());
					
					java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) sb.append(line);
					
					final String jsonResponse = sb.toString();
					
					runOnUiThread(new Runnable() {
						@Override public void run() {
							try {
								// إزالة رسالة "جاري البحث"
								if (chatList.size() > 0) chatList.remove(chatList.size() - 1);
								
								JSONObject obj = new JSONObject(jsonResponse);
								String answerText = obj.getString("text");
								String answerImage = obj.getString("image");
								
								// عرض النتيجة النهائية مع الصورة إن وجدت
								addMessage(answerText, false, answerImage);
								
								// حفظ في سجل المحادثات
								saveChatToMySQL(query, answerText);
							} catch (Exception e) {
								addMessage("خطأ في معالجة البيانات من السيرفر.", false);
							}
						}
					});
				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						@Override public void run() {
							if (chatList.size() > 0) chatList.remove(chatList.size() - 1);
							addMessage("❌ تعذر الاتصال بالسيرفر المحلى.", false);
						}
					});
				}
			}
		}).start();
	}
	
	// ==========================================
	// 4. دالة الحفظ والجلب (MySQL)
	// ==========================================
	private void saveChatToMySQL(final String userMsg, final String aiRes) {
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					java.net.URL url = new java.net.URL(server_url);
					java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					String postData = "action=save_chat" + 
					"&message=" + java.net.URLEncoder.encode(userMsg, "UTF-8") + 
					"&response=" + java.net.URLEncoder.encode(aiRes, "UTF-8");
					conn.getOutputStream().write(postData.getBytes());
					conn.getInputStream().read(); 
					conn.disconnect();
				} catch (Exception e) {}
			}
		}).start();
	}
	
	private void fetchChatHistory() {
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					java.net.URL url = new java.net.URL(server_url);
					java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.getOutputStream().write("action=get_all".getBytes());
					
					java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) sb.append(line);
					final String result = sb.toString();
					
					runOnUiThread(new Runnable() {
						@Override public void run() {
							try {
								JSONArray array = new JSONArray(result);
								chatList.clear(); 
								for(int i=0; i<array.length(); i++) {
									JSONObject obj = array.getJSONObject(i);
									addMessage(obj.getString("message"), true);
									addMessage(obj.getString("response"), false);
								}
							} catch (Exception e) {}
						}
					});
				} catch (Exception e) {}
			}
		}).start();
	}
	
	// ==========================================
	// 5. إعداد المحول (Adapter) لعرض النصوص والصور
	// ==========================================
	private void setupChatAdapter() {
		adapter = new BaseAdapter() {
			@Override public int getCount() { return chatList.size(); }
			@Override public Object getItem(int p) { return chatList.get(p); }
			@Override public long getItemId(int p) { return p; }
			@Override public View getView(int pos, View v, ViewGroup pr) {
				if (v == null) v = getLayoutInflater().inflate(R.layout.chat_item, null);
				HashMap<String, Object> map = chatList.get(pos);
				
				TextView body = v.findViewById(R.id.text_message_body);
				LinearLayout bg = v.findViewById(R.id.linear13);
				
				// حذف أي صورة سابقة لمنع التداخل أثناء التمرير
				for(int i = bg.getChildCount()-1; i >= 0; i--) {
					View child = bg.getChildAt(i);
					if(child instanceof ImageView) { bg.removeViewAt(i); }
				}
				
				String txt = map.containsKey("text") ? String.valueOf(map.get("text")) : "";
				boolean isU = map.containsKey("isUser") && String.valueOf(map.get("isUser")).equals("true");
				String imgB64 = map.containsKey("image") ? String.valueOf(map.get("image")) : "";
				
				body.setText(txt);
				
				// تصميم الفقاعات
				android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
				gd.setCornerRadius(30);
				if (isU) {
					gd.setColor(Color.parseColor("#E9F5EE"));
					bg.setGravity(Gravity.RIGHT);
				} else {
					gd.setColor(Color.WHITE);
					gd.setStroke(2, Color.parseColor("#EEEEEE"));
					bg.setGravity(Gravity.LEFT);
				}
				body.setBackground(gd);
				
				// عرض الصورة من قاعدة البيانات (إذا وجدت)
				if (imgB64 != null && !imgB64.isEmpty() && !imgB64.equals("null")) {
					try {
						byte[] decodedString = android.util.Base64.decode(imgB64, android.util.Base64.DEFAULT);
						android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
						if (decodedByte != null) {
							ImageView iv = new ImageView(MainActivity.this);
							iv.setImageBitmap(decodedByte);
							iv.setAdjustViewBounds(true);
							LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
							p.setMargins(10, 10, 10, 10);
							iv.setLayoutParams(p);
							bg.addView(iv);
						}
					} catch (Exception e) {}
				}
				return v;
			}
		};
		listview_chat.setAdapter(adapter);
		fetchChatHistory();
	}
	
	// دالة الربط مع زر الإرسال
	private void startAnalysis(String q) {
		searchDatabase(q);
	}
	
	private void initializeLogic_Dummy() {
		// هذه الدالة وهمية لفتح قوس onCreate مرة أخرى لسكيتشوير
	}
	
	public class Listview_chatAdapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Listview_chatAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.chat_item, null);
			}
			
			final LinearLayout linear13 = _view.findViewById(R.id.linear13);
			final TextView text_message_body = _view.findViewById(R.id.text_message_body);
			final LinearLayout linear14 = _view.findViewById(R.id.linear14);
			final ImageView btn_retry = _view.findViewById(R.id.btn_retry);
			final ImageView btn_like = _view.findViewById(R.id.btn_like);
			final ImageView btn_share = _view.findViewById(R.id.btn_share);
			final ImageView btn_copy = _view.findViewById(R.id.btn_copy);
			
			return _view;
		}
	}
}