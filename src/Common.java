import java.math.BigDecimal;
import java.util.Comparator;
public class Common {
	
	public static boolean isNum(String str){
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

}

class MyCompare implements Comparator //ʵ��Comparator�������Լ��ıȽϷ���
{
public int compare(Object o1, Object o2) {
Object[] e1=(Object[])o1;
Object[] e2=(Object[])o2;

if((long)e1[1] > (long)e2[1])//�����Ƚ��ǽ���,�����-1�ĳ�1��������.
{
   return 1;
}
else
{
   return -1;
}
}
}



class CompareStr implements Comparator //ʵ��Comparator�������Լ��ıȽϷ���
{
public int compare(Object o1, Object o2) {
Object[] e1=(Object[])o1;
Object[] e2=(Object[])o2;

if(Long.parseLong((String)e1[1]) > Long.parseLong((String)e2[1]))//�����Ƚ��ǽ���,�����-1�ĳ�1��������.
{
   return -1;
}
else
{
   return 1;
}
}
}

