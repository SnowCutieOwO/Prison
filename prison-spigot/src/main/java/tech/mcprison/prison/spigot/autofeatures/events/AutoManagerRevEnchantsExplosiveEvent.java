package tech.mcprison.prison.spigot.autofeatures.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import me.revils.revenchants.events.ExplosiveEvent;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.autofeatures.AutoFeaturesFileConfig.AutoFeatures;
import tech.mcprison.prison.mines.features.MineBlockEvent.BlockEventType;
import tech.mcprison.prison.output.ChatDisplay;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.output.Output.DebugTarget;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.api.PrisonMinesBlockBreakEvent;
import tech.mcprison.prison.spigot.autofeatures.AutoManagerFeatures;
import tech.mcprison.prison.spigot.block.BlockBreakPriority;
import tech.mcprison.prison.spigot.game.SpigotHandlerList;

public class AutoManagerRevEnchantsExplosiveEvent 
	extends AutoManagerFeatures
	implements PrisonEventManager
{

	private BlockBreakPriority bbPriority;
	
	public AutoManagerRevEnchantsExplosiveEvent() {
		super();
	}
	
	public AutoManagerRevEnchantsExplosiveEvent( BlockBreakPriority bbPriority ) {
		super();
		
		this.bbPriority = bbPriority;
	}
	
	
	public BlockBreakPriority getBbPriority() {
		return bbPriority;
	}
	public void setBbPriority( BlockBreakPriority bbPriority ) {
		this.bbPriority = bbPriority;
	}
	
	@Override
	public void registerEvents() {
		
		initialize();
		
	}

		
	public class AutoManagerRevEnchantsExplosiveEventListener
		extends AutoManagerRevEnchantsExplosiveEvent
		implements Listener {
    	
    	public AutoManagerRevEnchantsExplosiveEventListener( BlockBreakPriority bbPriority ) {
    		super( bbPriority );
    	}
    	
		@EventHandler(priority=EventPriority.NORMAL) 
		public void onRevEnchantsExplosive( 
				ExplosiveEvent e, BlockBreakPriority bbPriority) {
	
			if ( isDisabled( e.getPlayer().getLocation().getWorld().getName() ) ||
					bbPriority.isDisabled() ) {
				return;
			}
			
			handleRevEnchantsExplosiveEvent( e, bbPriority );
		}
	}
    

	@Override
	public void initialize() {

		String eP = getMessage( AutoFeatures.RevEnchantsExplosiveEventPriority );
		BlockBreakPriority bbPriority = BlockBreakPriority.fromString( eP );
		
		setBbPriority( bbPriority );
		
//		boolean isEventEnabled = eP != null && !"DISABLED".equalsIgnoreCase( eP );
		
		if ( getBbPriority() == BlockBreakPriority.DISABLED ) {
			return;
		}
		
		// Check to see if the class ExplosiveEvent even exists:
		try {
			Output.get().logInfo( "AutoManager: checking if loaded: RevEnchants ExplosiveEvent" );
			
			Class.forName( "me.revils.revenchants.events.ExplosiveEvent", false, 
					this.getClass().getClassLoader() );
			
			Output.get().logInfo( "AutoManager: Trying to register RevEnchants ExplosiveEvent" );
			
			
			SpigotPrison prison = SpigotPrison.getInstance();
			PluginManager pm = Bukkit.getServer().getPluginManager();
			EventPriority ePriority = getBbPriority().getBukkitEventPriority(); 
			
			
			AutoManagerRevEnchantsExplosiveEventListener autoManagerlListener = 
					new AutoManagerRevEnchantsExplosiveEventListener( bbPriority );
			
			pm.registerEvent(
					ExplosiveEvent.class, 
					autoManagerlListener, ePriority,
					new EventExecutor() {
				public void execute(Listener l, Event e) { 
					
					ExplosiveEvent exEvent = (ExplosiveEvent) e;
					
					((AutoManagerRevEnchantsExplosiveEventListener)l)
									.onRevEnchantsExplosive( exEvent, getBbPriority() );
				}
			},
					prison);
			prison.getRegisteredBlockListeners().add( autoManagerlListener );
			
			
		}
		catch ( ClassNotFoundException e ) {
			// CrazyEnchants is not loaded... so ignore.
			Output.get().logInfo( "AutoManager: RevEnchants ExplosiveEvent is not loaded" );
		}
		catch ( Exception e ) {
			Output.get().logInfo( "AutoManager: RevEnchants ExplosiveEvent failed to load. [%s]", e.getMessage() );
		}
	}
   
	
    @Override
    public void unregisterListeners() {
    	
//    	super.unregisterListeners();
    }
	
	@Override
	public void dumpEventListeners() {
		
		StringBuilder sb = new StringBuilder();
		
		dumpEventListeners( sb );
		
		if ( sb.length() > 0 ) {

			
			for ( String line : sb.toString().split( "\n" ) ) {
				
				Output.get().logInfo( line );
			}
		}
		
	}
    
	
	@Override
	public void dumpEventListeners( StringBuilder sb ) {
		
		String eP = getMessage( AutoFeatures.RevEnchantsExplosiveEventPriority );
		boolean isEventEnabled = eP != null && !"DISABLED".equalsIgnoreCase( eP );

		if ( !isEventEnabled ) {
			return;
		}
		
		// Check to see if the class BlastUseEvent even exists:
		try {
			
			Class.forName( "me.revils.revenchants.events.ExplosiveEvent", false, 
					this.getClass().getClassLoader() );
			
    		BlockBreakPriority bbPriority = BlockBreakPriority.fromString( eP );

			
			String title = String.format( 
					"ExplosiveEvent (%s)", 
					( bbPriority == null ? "--none--" : bbPriority.name()) );
			
			ChatDisplay eventDisplay = Prison.get().getPlatform().dumpEventListenersChatDisplay( 
					title, 
					new SpigotHandlerList( ExplosiveEvent.getHandlerList()) );
			
			if ( eventDisplay != null ) {
				sb.append( eventDisplay.toStringBuilder() );
				sb.append( "\n" );
			}
		}
		catch ( ClassNotFoundException e ) {
			// CrazyEnchants is not loaded... so ignore.
		}
		catch ( Exception e ) {
			Output.get().logInfo( "AutoManager: RevEnchants ExplosiveEvent failed to load. [%s]", e.getMessage() );
		}
	}


	/**
	 * <p>Since there are multiple blocks associated with this event, pull out the player first and
	 * get the mine, then loop through those blocks to make sure they are within the mine.
	 * </p>
	 * 
	 * <p>The logic in this function is slightly different compared to genericBlockEvent() because this
	 * event contains multiple blocks so it's far more efficient to process the player data once. 
	 * So that basically needed a slight refactoring.
	 * </p>
	 * 
	 * @param e
	 */
	public void handleRevEnchantsExplosiveEvent( ExplosiveEvent e, BlockBreakPriority bbPriority ) {
			
//			boolean monitor, boolean blockEventsOnly, 
//			boolean autoManager ) {

		long start = System.nanoTime();
		
		// If the event is canceled, it still needs to be processed because of the 
		// MONITOR events:
		// An event will be "canceled" and "ignored" if the block 
		// BlockUtils.isUnbreakable(), or if the mine is actively resetting.
		// The event will also be ignored if the block is outside of a mine
		// or if the targetBlock has been set to ignore all block events which 
		// means the block has already been processed.
    	MinesEventResults eventResults = ignoreMinesBlockBreakEvent( e, 
    							e.getPlayer(),  e.getBlocks().get( 0 ) );
		if ( eventResults.isIgnoreEvent() ) {
			return;
		}
//		if ( ignoreMinesBlockBreakEvent( e, e.getPlayer(), e.getBlocks().get( 0 )) ) {
//    		return;
//    	}
    	
		
		// Register all external events such as mcMMO and EZBlocks:
//		OnBlockBreakExternalEvents.getInstance().registerAllExternalEvents();
				
		StringBuilder debugInfo = new StringBuilder();
		
		debugInfo.append( String.format( "### ** handleRevEnchantsExplosiveEvent ** ### " +
				"(event: ExplosiveEvent, config: %s, priority: %s, canceled: %s) ",
				bbPriority.name(),
				bbPriority.getBukkitEventPriority().name(),
				(e.isCancelled() ? "TRUE " : "FALSE")
				) );
		
		
		// NOTE that check for auto manager has happened prior to accessing this function.
		
		// Process all priorities if the event has not been canceled, and 
		// process the MONITOR priority even if the event was canceled:
    	if ( !bbPriority.isMonitor() && !e.isCancelled() || 
    			bbPriority.isMonitor() &&
    			e.getBlocks().size() > 0 ) {

		
    		// if event listener is disabled then this code should never be reachable:
//    		boolean isEventListenerEnabled = bbPriority == BlockBreakPriority.DISABLED;
    		
//	    	String reEx = getMessage( AutoFeatures.RevEnchantsExplosiveEventPriority );
//			boolean isREExplosiveEnabled = reEx != null && !"DISABLED".equalsIgnoreCase( reEx );

			
    		Block bukkitBlock = e.getBlocks().get( 0 );
    		
    		// Need to wrap in a Prison block so it can be used with the mines:
//    		SpigotBlock sBlock = SpigotBlock.getSpigotBlock( bukkitBlock );
//    		SpigotPlayer sPlayer = new SpigotPlayer(e.getPlayer());
    		
    		BlockEventType eventType = BlockEventType.RevEnExplosion;
    		String triggered = null;
    		

    		PrisonMinesBlockBreakEvent pmEvent = new PrisonMinesBlockBreakEvent( 
    					bukkitBlock, 
    					e.getPlayer(),
    					eventResults.getMine(),
//    					sBlock, sPlayer, 
    					bbPriority, eventType, triggered,
    					debugInfo );
		
    		
    		for ( int i = 1; i < e.getBlocks().size(); i++ ) {
    			pmEvent.getUnprocessedRawBlocks().add( e.getBlocks().get( i ) );
    		}
    		
    		
    		if ( !validateEvent( pmEvent ) ) {
    			
    			// The event has not passed validation. All logging and Errors have been recorded
    			// so do nothing more. This is to just prevent normal processing from occurring.
    			
    			if ( pmEvent.isCancelOriginalEvent() ) {
    				
    				e.setCancelled( true );
    			}
    			
    			debugInfo.append( "(doAction failed validation) " );
    		}

    		

    		// The validation was successful, but stop processing for the MONITOR priorities.
    		// Note that BLOCKEVENTS processing occured already within validateEvent():
    		else if ( pmEvent.getBbPriority().isMonitor() ) {
    			// Stop here, and prevent additional processing. 
    			// Monitors should never process the event beyond this.
    		}
    		

    		
    		// This is where the processing actually happens:
    		else {
    			
//    			debugInfo.append( "(normal processing initiating) " );
    			
    			// check all external events such as mcMMO and EZBlocks:
//    			if ( e instanceof BlockBreakEvent ) {
//    				processPMBBExternalEvents( pmEvent, debugInfo, e );
//    			}
//    			
    			
    			EventListenerCancelBy cancelBy = EventListenerCancelBy.none; 
    			
    			cancelBy = processPMBBEvent( pmEvent );

    			
    			if ( cancelBy != EventListenerCancelBy.none ) {
    				
    				e.setCancelled( true );
    				debugInfo.append( "(event canceled) " );
    			}
//    			else if ( cancelBy == EventListenerCancelBy.drops ) {
//					try
//					{
//						e.setDropItems( false );
//						debugInfo.append( "(drop canceled) " );
//					}
//					catch ( NoSuchMethodError e1 )
//					{
//						String message = String.format( 
//								"Warning: The autoFeaturesConfig.yml setting `cancelAllBlockEventBlockDrops` " +
//										"is not valid for this version of Spigot. It's only vaid for spigot v1.12.x and higher. " +
//										"Modify the config settings and set this value to `false`.  For now, it is temporarily " +
//										"disabled. [%s]",
//										e1.getMessage() );
//						Output.get().logWarn( message );
//						
//						AutoFeaturesWrapper.getInstance().getAutoFeaturesConfig()
//								.setFeature( AutoFeatures.cancelAllBlockEventBlockDrops, false );
//					}
//
//    			}
    		}

//    		// now process all blocks (non-monitor):
//    		else if ( pmEvent.getMine() != null || 
//    				pmEvent.getMine() == null && !isBoolean( AutoFeatures.pickupLimitToMines ) ) {
//
//
//    			if ( pmEvent.getExplodedBlocks().size() > 0 ) {
//    				
////					String triggered = null;
//    				
//					
//					// Warning: BlastUseEvent does not identify the block the player actually hit, so the dummyBlock
//					//          is just a random first block from the explodedBlocks list and may not be the block
//					//          that initiated the explosion event.
////					SpigotBlock dummyBlock = explodedBlocks.get( 0 );
//					
////	    			PrisonMinesBlockBreakEvent pmbbEvent = new PrisonMinesBlockBreakEvent( dummyBlock.getWrapper(), e.getPlayer(),
////	    												mine, dummyBlock, explodedBlocks, BlockEventType.CEXplosion, triggered );
//	                Bukkit.getServer().getPluginManager().callEvent(pmEvent);
//	                if ( pmEvent.isCancelled() ) {
//	                	debugInfo.append( "(normal processing: PrisonMinesBlockBreakEvent was canceled) " );
//	                }
//	                else {
//	                	
////	                	// Cancel drops if so configured:
////	                	if ( isBoolean( AutoFeatures.cancelAllBlockEventBlockDrops ) ) {
////	                		
////	                		try
////	                		{
////	                			e.setDropItems( false );
////	                		}
////	                		catch ( NoSuchMethodError e1 )
////	                		{
////	                			String message = String.format( 
////	                					"Warning: The autoFeaturesConfig.yml setting `cancelAllBlockEventBlockDrops` " +
////	                					"is not valid for this version of Spigot. Modify the config settings and set " +
////	                					"this value to `false`. [%s]",
////	                					e1.getMessage() );
////	                			Output.get().logWarn( message );
////	                		}
////	                	}
//	                	
//	                	if ( doAction( pmEvent, debugInfo ) ) {
//	                		
//	                		if ( isBoolean( AutoFeatures.cancelAllBlockBreakEvents ) ) {
//	                			
//	                			e.setCancelled( true );
//	                		}
//	                		else {
//	                			
//	                			debugInfo.append( "(event was not canceled) " );
//	                		}
//	                		
//	                		finalizeBreakTheBlocks( pmEvent );
//	                		
//	                		doBlockEvents( pmEvent );
//
//	                	}
//	                	
//	                	else {
//	                		
//	                		debugInfo.append( "(doAction failed without details) " );
//	                	}
//	                	
//	                }
//    			}
//    			
//
//    			debugInfo.append( "(normal processing) " );
//    		}
//    		else {
//    			
//    			debugInfo.append( "(logic bypass) " );
//    		}

		}
    	
		if ( debugInfo.length() > 0 ) {
			
			long stop = System.nanoTime();
			debugInfo.append( " [" ).append( (stop - start) / 1000000d ).append( " ms]" );
			
			Output.get().logDebug( DebugTarget.blockBreak, debugInfo.toString() );
		}
	}

	@Override
	protected int checkBonusXp( Player player, Block block, ItemStack item ) {
		int bonusXp = 0;
		
		return bonusXp;
	}
	
//	@Override
//	protected int checkBonusXp( Player player, Block block, ItemStack item ) {
//		int bonusXp = 0;
//		
//		try {
//			if ( isRevEnchantsExplosiveEventEnabled() == null ) {
//				Class.forName( 
//						"tech.mcprison.prison.spigot.integrations.IntegrationCrazyEnchantmentsPickaxes", false, 
//						this.getClass().getClassLoader() );
//				setRevEnchantsExplosiveEventEnabled( Boolean.TRUE );
//			}
//			
//			if ( isRevEnchantsExplosiveEventEnabled() != null && isRevEnchantsExplosiveEventEnabled().booleanValue() && 
//					item != null && IntegrationCrazyEnchantmentsPickaxes.getInstance().isEnabled() ) {
//				
//				bonusXp = IntegrationCrazyEnchantmentsPickaxes.getInstance()
//						.getPickaxeEnchantmentExperienceBonus( player, block, item );
//			}
//		}
//		catch ( NoClassDefFoundError | Exception e ) {
//			setRevEnchantsExplosiveEventEnabled( Boolean.FALSE );
//		}
//		
//		return bonusXp;
//	}


}
