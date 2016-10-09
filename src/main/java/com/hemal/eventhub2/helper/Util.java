package com.hemal.eventhub2.helper;

/**
 * Created by Hemal on 09-Oct-16.
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;


public class Util {


	public static String getStringFromURL(String s,HashMap<String,String> hashMap){
		try {
			URL url=new URL(s);
			HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.connect();

			String data = "";

			for (String key:hashMap.keySet()){

			}
			int len = hashMap.size();
			Set<String> set = hashMap.keySet();
			java.util.Iterator ir = set.iterator();

			for(int i=0;i<len;i++) {
				String key = (String) ir.next();
				//
				String value = hashMap.get(key);
//                Log.d("testing_data"+i,key+" "+value);
				Log.d("Key and value",key+" "+value);
				if(i==0)
					data = ""+key+"="+URLEncoder.encode(value,"UTF-8");
				else
					data +="&"+key+"="+URLEncoder.encode(value,"UTF-8");
			}

			Log.d("checking_send_data",data);
			OutputStreamWriter out = new OutputStreamWriter(httpURLConnection.getOutputStream());
			out.write(data);
			out.close();
			int HttpResult =httpURLConnection.getResponseCode();
			if(HttpResult ==HttpURLConnection.HTTP_OK) {
				InputStream is = httpURLConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
//                Log.d("checking_val",builder.toString());
				return builder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


//    public static Bitmap getDrawableFromURL(String s,HashMap<String,String> hashMap){
//
//        String img=getStringFromURL(s, hashMap);
//        if (img!=null && img.length()>100) {
////        String img="/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgBBgkBBwgKCgkLDRYPDQwMDRsUFRAWIB0iIiAdHx8kKDQsJCYxJx8fLT0tMTU3Ojo6Iys/RD84QzQ5OjcBCgoKDQwNGg8PGjclHyU3Nzc3NzcsNzc3Nzc3LDU0NzQ3NzcvNDcyNzI3NTY0Nzc3Njg3MjQ2Njc0Njc2NDI0N//AABEIACAAIAMBEQACEQEDEQH/xAAZAAEBAAMBAAAAAAAAAAAAAAAGBAMFBwL/xAAxEAABAgMFBgQGAwAAAAAAAAABAgMEBhEABQcTIQgxQWGBsRIzUpEUFkJRccEXGCL/xAAZAQEBAQEBAQAAAAAAAAAAAAAFBAMGAQD/xAAnEQABAwIGAQQDAAAAAAAAAAABAAIDBBESEyExQWEFIlFx0RSB8P/aAAwDAQACEQMRAD8A6pNExt7H7TcttN58c/5TNaCnqVy72wnnbC25/S0iiMhsEPiom9JkWiab6mH4EuaoaQ/kCnIDWn5raMCsnFxoOvtbl1NEbHftXNw1+4Mn5rgr3iHTv8EQsupV78PxS0+fUxG1724P2tsEMg2t8JLLt/J2mkuSU+1kRzHmtVqCPUnl2snT1DJ23bvyPZRzQuidYoaVqxpnCPxOfFcp5TKAdQkIJSOlRXqbDVlQPy/VsP4q6KMiDTcrJckBHY3fG7JeTDQkVuvGPcVmvvpWNC2CNEkVA+32t0rXsc0PbqDshQx1y078q+7YoyCf6dxjzabwhKMhClBJfQB/haRxqka04g2E8lTS4s6IXB36V9JMwDLedQoVPqwmmm7cSmwQpx5LKwPqSshOvuD0Fj/HTkVI70V1QwOhPSyNtfxBNN4yG9oXHlPJJ4pWfEO5HQ2+8jFgqCeDqvIXY4R0qL9VFYM3nd2PcCos0bcadiBDqfBGhShSE676kGzHiXAwlhOxRdXcSBwWrgXE44TAqWYgxGex4I15+Mby3Yjw1CEto+hsVqda2uqpcqAvGvCwibjkAPyvYB2hZsu7D1jXLfS+sjglBCj2A6i3O+PhxTg8BMzPwwntMpiuJO00hubmHAxGs+W7SoI9KuXazdTTMqGYXKGGZ0TrhGlRMwbP6vlh+7Ypw7guHQXUnqn90sI6gqYj6dR0rcyCTmykU3fO0zGtzmzcjyYttCm0RMSFMhCVbwa08Q6G1dOyryzERZp91i8U4eH3uR7JZK0uN7HbTkwOuZ8a95r1KAD0p5d7I09OyBuFqnllMhuV/9k";
//            byte[] decodedString = Base64.decode(img, Base64.NO_WRAP);
//            InputStream inputStream = new ByteArrayInputStream(decodedString);
//            return BitmapFactory.decodeStream(inputStream);
//        }
//        return null;
//    }



	public static String getStringFromURL(String s) {
		try {
			URL url=new URL(s);
			HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
			httpURLConnection.connect();
			InputStream is=httpURLConnection.getInputStream();

			BufferedReader reader=new BufferedReader(new InputStreamReader(is));


			String line;
			StringBuilder builder=new StringBuilder();
			while((line=reader.readLine())!=null){
				builder.append(line);
			}
			return builder.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
