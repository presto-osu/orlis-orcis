package io.github.otakuchiyan.dnsman;

public class IPChecker
{
	public static boolean isIPv4(String ip){
		if(!ip.equals("")){
			for(int i = 1; i != ip.length(); i++){
				if(ip.charAt(i - 1) == '.'){
					return true;
				}
			}
		}
		return false;
	}
	public static boolean IPv4Checker(String ip){
		if(!ip.equals("")){
			int dotCount = 0;
			//Detecting user inputed a dot as first char
			if(ip.charAt(0) == '.'){
				return false;
			}
			for(int i = 1; i != ip.length(); i++){
				if(ip.charAt(i - 1) == '.'
						//Detecting next dot
						&& !ip.substring(i, i + 1).equals(".")){
					dotCount++;
					if(dotCount == 3){
						//if i is a dot return false
						return i != ip.length();
					}
				}
			}
		}
		return false;
	}

    public static boolean IPv6Checker(String ip){
		if(!ip.equals("")){
			int colonCount = 0;
			for(int i = 0; i != ip.length(); i++){
				if(ip.charAt(i) == ':'){
					colonCount++;
					if(colonCount >= 2){
						//if i is a colon return false
						return (i + 1) != ip.length();
					}
				}else if(ip.charAt(i) == '.'){
					return false;
				}
			}
		}

		return false;
    }
}
