package midp.tapeline.midlegram.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import midp.tapeline.midlegram.ArrayUtils;
import midp.tapeline.midlegram.client.data.Chat;

public class MGClient {
	
	private String sessionKey;
	private String url;

	public MGClient(String url, String sessionKey) {
		this.url = url;
		this.sessionKey = sessionKey;
	}
	
	public void startAuth(String phone) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = (HttpConnection) Connector.open(
					url + "/api/account/login/phone?phone=" + phone, Connector.READ_WRITE, true);
			conn.setRequestProperty("X-Phone", phone);
			conn.setRequestMethod("POST");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			sessionKey = des.readString();
			System.out.println(sessionKey);
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public void confirmCode(String code) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("POST", "/api/account/login/code?code=" + code);
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			byte verdict = dis.readByte();
			if (verdict != 0) throw new InvalidCodeException();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Vector getFolders() throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/folders");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readFolderList();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Vector getChatsIds(long folderId) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/folders/" + folderId + "/chats_ids");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readIdList();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Vector getChats(long folderId) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/folders/" + folderId + "/chats");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readChatList();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Chat getChat(long chatId) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/chats/" + chatId);
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readChat();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Vector pollUpdates() throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/updates");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readMessageList();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public void connectClient() throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("POST", "/api/connect");
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}
	
	public Vector getMessages(long chatId, long fromMsgId, int count) throws IOException {
		HttpConnection conn = null;
		DataInputStream dis = null;
		try {
			conn = openSessionHttp("GET", "/api/chats/" + chatId + "/messages?from_msg=" + fromMsgId + "&lim=" + count);
			assertRespOk(conn);
			dis = conn.openDataInputStream();
			Deserializer des = new Deserializer(dis);
			assertOpSuccess(des.readOperationSuccess());
			return des.readMessageList();
		} finally { 
			if (dis != null) dis.close(); 
			if (conn != null) conn.close();
		}
	}

	private HttpConnection openSessionHttp(String method, String path) throws IOException {
		HttpConnection conn = (HttpConnection) Connector.open(url + path, Connector.READ_WRITE, true);
		conn.setRequestProperty("Authorization", sessionKey);
		conn.setRequestMethod(method);
		return conn;
	}
	
	private void assertOpSuccess(boolean success) throws IOException {
		if (!success) throw new ClientException("Operation wasn't successful");
	}
	
	private void assertRespOk(HttpConnection conn) throws IOException {
		int code = conn.getResponseCode();
		if (code / 100 != 2) throw new ClientException("Unexpected response " + code);
	}
	
	public static class ClientException extends IOException {
		public ClientException(String message) {
			super(message);
		}
	}
	
	public static class InvalidCodeException extends ClientException {
		public InvalidCodeException() {
			super("Invalid code");
		}
	}
	
}
