public class ChatBot
{
    public static void main(String[] args) throws Exception
    {
        bot ChatBot = new bot();
        ChatBot.setVerbose(true);
        ChatBot.connect("irc.freenode.net");
        ChatBot.joinChannel("#chatBotTest");
        ChatBot.sendMessage("#chatBotTest", "Enter 'weather' followed by zipcode, city or 'symbol' followed by a stock symbol to return the temperature or finances.");
    }
}
