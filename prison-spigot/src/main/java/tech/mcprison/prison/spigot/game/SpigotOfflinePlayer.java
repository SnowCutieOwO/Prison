package tech.mcprison.prison.spigot.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.ItemStack;
import tech.mcprison.prison.internal.OfflineMcPlayer;
import tech.mcprison.prison.internal.inventory.Inventory;
import tech.mcprison.prison.internal.scoreboard.Scoreboard;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.util.Gamemode;
import tech.mcprison.prison.util.Location;

public class SpigotOfflinePlayer
	implements OfflineMcPlayer {
	
	private OfflinePlayer offlinePlayer;
	
	public SpigotOfflinePlayer(OfflinePlayer offlinePlayer) {
		this.offlinePlayer = offlinePlayer;
	}

	@Override
	public String getName() {
		return offlinePlayer.getName();
	}

	@Override
	public void dispatchCommand( String command ) {
		
	}

	@Override
	public UUID getUUID() {
		return offlinePlayer.getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return offlinePlayer.getName();
	}

	@Override
	public boolean isOnline() {
		return offlinePlayer.isOnline();
//		return false;
	}
	
	/**
	 * NOTE: A SpigotOfflinePlayer does not represent an online player with inventory.  
	 *       This class is not "connected" to the underlying bukkit player
	 *       so technically this is not a player object, especially since it
	 *       always represents offline players too.
	 */
    @Override 
    public boolean isPlayer() {
    	return (offlinePlayer.getPlayer() instanceof Player );
//    	return false;
    }
    
//	@Override
//	public boolean hasPermission( String perm ) {
//		Output.get().logError( "SpigotOfflinePlayer.hasPermission: Cannot access permissions for offline players." );
//		return false;
//	}
	
	@Override
	public void setDisplayName( String newDisplayName ) {
		Output.get().logError( "SpigotOfflinePlayer.setDisplayName: Cannot set display names." );
	}
	
	@Override
	public void sendMessage( String message ) {
		Output.get().logError( "SpigotOfflinePlayer.sendMessage: Cannot send messages to offline players." );
	}
	
	@Override
	public void sendMessage( String[] messages ) {
		Output.get().logError( "SpigotOfflinePlayer.sendMessage: Cannot send messages to offline players." );
	}
	
	@Override
	public void sendRaw( String json ) {
		Output.get().logError( "SpigotOfflinePlayer.sendRaw: Cannot send messages to offline players." );
	}

	@Override
	public boolean doesSupportColors() {
		return false;
	}

	@Override
	public void give( ItemStack itemStack ) {
		Output.get().logError( "SpigotOfflinePlayer.give: Cannot give to offline players." );
	}

	@Override
	public Location getLocation() {
		Output.get().logError( "SpigotOfflinePlayer.getLocation: Offline players have no location." );
		return null;
	}

	@Override
	public void teleport( Location location ) {
		Output.get().logError( "SpigotOfflinePlayer.teleport: Offline players cannot be teleported." );
	}

	@Override
	public void setScoreboard( Scoreboard scoreboard ) {
		Output.get().logError( "SpigotOfflinePlayer.setScoreboard: Offline players cannot use scoreboards." );
	}

	@Override
	public Gamemode getGamemode() {
		Output.get().logError( "SpigotOfflinePlayer.getGamemode: Offline is not a valid gamemode." );
		return null;
	}

	@Override
	public void setGamemode( Gamemode gamemode ) {
	}

	@Override
	public Optional<String> getLocale() {
		Output.get().logError( "SpigotOfflinePlayer.getLocale: Offline is not a valid gamemode." );
		return null;
	}

	@Override
	public boolean isOp() {
		return offlinePlayer.isOp();
	}

	@Override
	public void updateInventory() {
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	@Override
	public void printDebugInventoryInformationToConsole() {
		
	}
	
	public OfflinePlayer getWrapper() {
		return offlinePlayer;
	}

	/**
	 * <p>Technically, offline players do not have any perms through bukkkit so
	 * bukkit cannot recalculate any permissions.
	 * </p>
	 */
	@Override
	public void recalculatePermissions() {
		
//		offlinePlayer.getPlayer().recalculatePermissions();
	}
	
    @Override
    public List<String> getPermissions() {
    	List<String> results = new ArrayList<>();
    	
    	if ( offlinePlayer.getPlayer() != null ) {
    		
    		Set<PermissionAttachmentInfo> perms = offlinePlayer.getPlayer().getEffectivePermissions();
    		for ( PermissionAttachmentInfo perm : perms )
    		{
    			results.add( perm.getPermission() );
    		}
    	}
    	else {
    		// try to use vault:
    		
    		// TODO add permission integrations here!!
    	}
    	
    	
    	return results;
    }
    
    @Override
    public List<String> getPermissions( String prefix ) {
    	List<String> results = new ArrayList<>();
    	
    	for ( String perm : getPermissions() ) {
			if ( perm.startsWith( prefix ) ) {
				results.add( perm );
			}
		}
    	
    	return results;
    }
    
	@Override
	public boolean hasPermission( String perm ) {
		boolean hasPerm = false;
		
		if ( offlinePlayer.getPlayer() != null ) {
			
			hasPerm = offlinePlayer.getPlayer().hasPermission( perm );
		}
		else {
			List<String> perms = getPermissions( perm );
			hasPerm = perms.contains( perm );
		}
		
		return hasPerm;
		
//		List<String> perms = getPermissions( perm );
//		return perms.contains( perm );
	}
    
//    @Override
//    public List<String> getPermissions() {
//    	List<String> results = new ArrayList<>();
//    	
//    	return results;
//    }
//    
//    @Override
//    public List<String> getPermissions( String prefix ) {
//    	List<String> results = new ArrayList<>();
//    	
//    	for ( String perm : getPermissions() ) {
//			if ( perm.startsWith( prefix ) ) {
//				results.add( perm );
//			}
//		}
//    	
//    	return results;
//    }
    
    
    /**
     * <p>This is not the active online player instance, so therefore prison would not have
     * access to the player's inventory if it is defaulting to this Player class. 
     * Therefore, this function should *never* be used in any calculations dealing with
     * the sales of inventory items.
     * </p>
     * 
     * <p>This is being set to a value of 1.0 so as not to change any other value that may
     * be used with this function.
     * </p>
     * 
     */
    @Override
    public double getSellAllMultiplier() {
    	double results = 1.0;
    	
    	Optional<tech.mcprison.prison.internal.Player> oPlayer = 
    									Prison.get().getPlatform().getPlayer( getName() );
    	
    	if ( oPlayer.isPresent() ) {
    		results = oPlayer.get().getSellAllMultiplier();
    	}
    	
    	return results;
    }
    
}
