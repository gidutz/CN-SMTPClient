
/**
 * SMTPSession example. Sends emails using SMTP protocol.
 * (c) 2002 by Svetlin Nakov
 */
 
 
public class SMTPClient
{
    public static void main(String[] args)
    {
        SMTPSession smtp = new SMTPSession(
           "compnet.idc.ac.il",
           "gidutz@gmail.com",
"tasker@cscidc.ac.il",
           "Some subject",
"... Message text ...");
        try {
           System.out.println("Sending e-mail...");
           smtp.sendMessage();
           System.out.println("E-mail sent.");
        } catch (Exception e) {
           smtp.close();
           System.out.println("Can not send e-mail!");
           e.printStackTrace();
        }
    }
}