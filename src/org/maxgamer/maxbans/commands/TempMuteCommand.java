package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Util;

public class TempMuteCommand extends CmdSkeleton{
    public TempMuteCommand(){
        super("tempmute", "maxbans.tempmute");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 2){
			String name = args[0];
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			boolean silent = Util.isSilent(args);
			if(name.isEmpty()){
				//sender.sendMessage(Formatter.primary + " No name given.");
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			String banner = Util.getName(sender);
			
			long time = Util.getTime(args);
			
			if(time <= 0){
				sender.sendMessage(getUsage());
				return true;
			}
			time += System.currentTimeMillis();
			
			//Make sure giving this mute will change something.
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				if(mute instanceof TempMute){
					TempMute tMute = (TempMute) mute;
					if(tMute.getExpires() > time){
						//sender.sendMessage(Formatter.primary + "That player already has a mute which lasts longer than the one you tried to give.");
						String msg = Msg.get("tempmute-shorter-than-last");
						sender.sendMessage(msg);
						return true;
					}
					//else: Continue normally.
				}
				else{
					//sender.sendMessage(Formatter.primary + "That player already has a mute which lasts longer than the one you tried to give.");
					String msg = Msg.get("tempmute-shorter-than-last");
					sender.sendMessage(msg);
					return true;
				}
			}
			
			String reason = Util.buildReason(args);
			
			plugin.getBanManager().tempmute(name, banner, reason, time);
			
			String until = Util.getTimeUntil(time);
			String message = Msg.get("announcement.player-was-temp-muted", new String[]{"banner", "name", "time", "reason"}, new String[]{banner, name, until, reason});
			plugin.getBanManager().addHistory(name, banner, message);
			plugin.getBanManager().announce(message, silent, sender);
			
	    	if(plugin.getSyncer() != null){
	    		Packet prop = new Packet();
	    		prop.setCommand("tempmute");
	    		prop.put("name", name);
	    		prop.put("banner", banner);
	    		prop.put("expires", time);
	    		prop.put("reason", reason);
	    		plugin.getSyncer().broadcast(prop);
	    		
	    		//Send the addhistory request.
	    		Packet history = new Packet().setCommand("addhistory").put("string", message).put("banner", banner).put("name", name);
	    		plugin.getSyncer().broadcast(history);
	    	}
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}
