import java.net.*;
import java.io.*;
import java.util.*;
import java.math.*;
import java.security.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class seizaServer {  // Echo Server (TCP)
  BufferedReader br;	// 文字入力ストリーム(受信用)
  PrintWriter pw;	// 文字出力ストリーム(送信用)
  String str;
  String str1;     //何座かどうか返信する
  InetAddress adrs;
  int port;
  boolean bl = true;
  boolean bl2 = true;

  seizaServer(Socket s) {	// コンストラクタ
  }

  public void seizaHandanTuki(int num1,int num2){
    //月の情報を受け取りどの星座か判別する
    switch(num1){
      case 1:
      if (num2 < 21) {
        str1 = "山羊";
      }else{
        str1 ="水瓶";
      }
      break;
      case 2:
      if (num2 < 20) {
        str1 = "水瓶";
      }else{
        str1 ="魚";
      }
      break;
      case 3:
      if (num2 < 21) {
        str1 = "魚";
      }else{
        str1 ="牡羊";
      }break;
      case 4:
      if (num2 < 21) {
        str1 = "牡羊";
      }else{
        str1 ="牡牛";
      }
      break;
      case 5:
      if (num2 < 21) {
        str1 = "牡牛";
      }else{
        str1 ="双子";
      }
      break;
      case 6:
      if (num2 < 22) {
        str1 = "双子";
      }else{
        str1 ="蟹";
      }
            break;
      case 7:
      if (num2 < 23) {
        str1 = "蟹";
      }else{
        str1 ="獅子";
      }
      break;
      case 8:
      if (num2 < 24) {
        str1 = "獅子";
      }else{
        str1 ="乙女";
      }
      break;
      case 9:
      if (num2 < 24) {
        str1 = "乙女";
      }else{
        str1 ="天秤";
      }
      break;
      case 10:
      if (num2 < 24) {
        str1 = "天秤";
      }else{
        str1 ="蠍";
      }
      break;
      case 11:
      if (num2 < 23) {
        str1 = "蠍";
      }else{
        str1 ="射手";
      }
      break;
      case 12:
      if (num2 < 223) {
        str1 = "射手";
      }else{
        str1 ="山羊";
      }
      break;
      default:
      bl2 = false;
      break;
    }

  }

  public void run() {	// メッセージ受信、返信処理
    //Socket s = clsock;
    int num1=0,num2=0;  //数字に変換した文字を格納する(月)（日）
    ServerSocket ss; Socket clsock;	// クライアントソケット
    Random rand = new Random();

      adrs = clsock.getInetAddress();	// クライアントのアドレス取得
      port = clsock.getPort();		// クライアントのポート取得
      System.err.print("Connect local ");
      System.err.print(clsock.getLocalAddress()+":"+ clsock.getLocalPort());
      System.err.println(", remote " + adrs + ":" + port);

      //br = new BufferedReader(new InputStreamReader(s.getInputStream()));
      //pw = new PrintWriter(new BufferedOutputStream(s.getOutputStream()), true);


      try {
        System.err.println("Echo (tcp/7777) server start.");
        // サーバソケット生成
        ss = new ServerSocket(7777);
        while (true) {	// 無限ループ
          clsock = ss.accept();	// 接続受け付け
          InputStream is = clsock.getInputStream();
          OutputStream os = clsock.getOutputStream();
          ObjectInputStream ois = new ObjectInputStream(is);
          ObjectOutputStream oos = new ObjectOutputStream(os);
          //クライアントの公開鍵を受信
          RSAPublicKey publicKey =
            (RSAPublicKey)ois.readObject();
          System.out.println("公開鍵を受け取った");
          //セッション鍵の生成
          BigInteger sessionNum = new BigInteger(64,rand);
          System.out.println("sessionNum="+sessionNum);
          //クライアントの公開鍵でセッション鍵を暗号化
          BigInteger encodedNum =
            sessionNum.modPow(publicKey.getPublicExponent(),
                              publicKey.getModulus());
          System.out.println("encodedNum="+encodedNum);

          //暗号化したセッション鍵をクライアントへ送信する
          oos.writeObject(encodedNum);
          //セッション鍵で暗号機を生成
          SecretKeyFactory keyFactory =
            SecretKeyFactory.getInstance("DES");
          DESKeySpec keySpec = new DESKeySpec(sessionNum.toByteArray());
          SecretKey sessionKey =
            keyFactory.generateSecret(keySpec);
          Cipher cipher =
            Cipher.getInstance("DES/CFB8/NoPadding");
          cipher.init(Cipher.ENCRYPT_MODE,sessionKey);
          //初期値IVを送信
          oos.write(cipher.getIV());
          oos.flush();oos = null;

        // メッセージの受信、返信
        for(int i=0;i<2;i++){
          str = br.readLine();
          System.err.println(port + ":" + str);
          if (bl) {
            //文字を数字に変換する(月)
            num1 = Integer.parseInt(str);
            bl = false;
          }
          //文字を数字に変換する(日)
          num2 = Integer.parseInt(str);
        }
        seizaHandanTuki(num1,num2);
        //暗号器で出力ストリームを暗号化
        ObjectOutputStream ocos = new ObjectOutputStream(
        new CipherOutputStream(os,cipher)
        );

        if (bl2) {
          //pw.println("あなたの星座は"+str1+"座です");
          ocos.writeObject("あなたの星座は"+str1+"座です");
        }else{
          //pw.println("何らかのエラーが発生しました");
          ocos.writeObject("何らかのエラーが発生しました");
        }


        //取得した星座を暗号化ストリームで送信
        ocos.close();ois.close();clsock.close();
      }

      }catch (Exception e) {
      e.printStackTrace();
  }
}

  public static void main(String args[]) {	// メイン処理

    try {
        new seizaServer.run();//星座判別
    } catch (Exception e) {
      e.printStackTrace();
  }
}
}
