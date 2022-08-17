package com.kazuha.jsbw;


import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.NextEvent;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.apache.logging.log4j.core.net.Priority;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.kazuha.jsbw.main.bedwarsAPI;
import static com.kazuha.jsbw.main.instance;

public class eventhandler implements Listener {
    List<Player> bridgeon = new ArrayList<>();

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void clickevents(PlayerInteractEvent e){
        if(!bedwarsAPI.getArenaUtil().getArenaByPlayer(e.getPlayer()).getStatus().equals(GameState.playing)){
            return;
        }
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(!bridgeon.contains(e.getPlayer())){
                return;
            }
            if(e.getPlayer().getItemInHand().getType() != Material.WOOL){
                return;
            }
            BukkitRunnable r = new BukkitRunnable(){
                @Override
                public void run(){
                    ITeam team = bedwarsAPI.getArenaUtil().getArenaByPlayer(e.getPlayer()).getTeam(e.getPlayer());
                    Location location = e.getClickedBlock().getLocation();
                    Location loc = e.getClickedBlock().getLocation();
                    if(loc.getBlock().getRelative(e.getBlockFace()).getType() != Material.WOOL){
                        if(e.getPlayer().getName().equals("KazuhaAyato")){
                            e.getPlayer().sendMessage("§cSTOPPED (E)");
                        }
                        return;
                    }
                    for(int i=2; i<=12; i++){
                        switch (e.getBlockFace()){
                            case UP:
                                loc = new Location(location.getWorld(),location.getX(),location.getY()+i, location.getZ());
                                break;
                            case DOWN:
                                loc = new Location(location.getWorld(),location.getX(),location.getY()-i, location.getZ());
                                break;
                            case NORTH:
                                loc = new Location(location.getWorld(),location.getX(),location.getY(), location.getZ()-i);
                                break;
                            case SOUTH:
                                loc = new Location(location.getWorld(),location.getX(),location.getY(), location.getZ()+i);
                                break;
                            case EAST:
                                loc = new Location(location.getWorld(),location.getX()+i,location.getY(), location.getZ());
                                break;
                            case WEST:
                                loc = new Location(location.getWorld(),location.getX()-i,location.getY(), location.getZ());
                                break;
                        }

                        Block block = loc.getBlock();
                        for(Player p : Bukkit.getOnlinePlayers()){
                            Location c = p.getLocation().add(0.0,1.0,0.0);
                            if(block.getRelative(e.getBlockFace()).equals(p.getLocation().getBlock())||block.getRelative(e.getBlockFace()).equals(c.getBlock())){
                                if(e.getPlayer().getName().equals("KazuhaAyato")){
                                    e.getPlayer().sendMessage("§cSTOPPED (R)");
                                }
                                return;
                            }
                        }
                        if(bedwarsAPI.getArenaUtil().getArenaByPlayer(e.getPlayer()).isProtected(block.getLocation())){
                            if(e.getPlayer().getName().equals("KazuhaAyato")){
                                e.getPlayer().sendMessage("§cSTOPPED (P)");
                            }
                            return;
                        }
                        if(block.getLocation().getBlock().getType() == Material.AIR) {
                            Bukkit.getScheduler().runTask(instance,()->{
                                block.setType(Material.WOOL);
                                block.setData(team.getColor().itemByte());
                                team.getArena().addPlacedBlock(block);
                            });
                            try {
                                Thread.sleep(100);
                                continue;
                            } catch (InterruptedException e) {
                                main.instance.getLogger().warning("发生错误:" + e.getLocalizedMessage());
                            }
                        }
                        return;
                    }

                }
            };
            r.runTaskLaterAsynchronously(instance, 3L);
        }
        if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR){
            if(e.getPlayer().getItemInHand().getType() == Material.WOOL){
                if(bridgeon.isEmpty()){
                    sendActionBar(e.getPlayer(),"§a§l已开启搭桥模式");
                    bridgeon.add(e.getPlayer());
                    return;
                }
                if(bridgeon.contains(e.getPlayer())){
                    sendActionBar(e.getPlayer(),"§c§l已关闭搭桥模式");
                    bridgeon.remove(e.getPlayer());
                }else{
                    sendActionBar(e.getPlayer(),"§a§l已开启搭桥模式");
                    bridgeon.add(e.getPlayer());
                }
            }
        }
    }
    @EventHandler
    public void onGameStateChangeInvent(GameStateChangeEvent e){
        if(!e.getNewState().equals(GameState.playing)) {
            return;
        }
        for(Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() != GameMode.SPECTATOR) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
            }
        }
        for(ITeam team : e.getArena().getTeams()){
            placebeddef(team);
        }
        e.getArena().setNextEvent(NextEvent.BEDS_DESTROY);
        e.getArena().updateNextEvent();
        BukkitRunnable ez = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    if (p.getGameMode() != GameMode.SPECTATOR) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 2));
                    }
                }
            }
        };
        ez.runTaskTimer(instance,10L,1L);
        for(IGenerator generator : e.getArena().getOreGenerators()){
            for(int i=0;i<=4;i++){
                generator.upgrade();
                e.getArena().updateNextEvent();
            }
        }
    }
    public void sendActionBar(Player player, String message){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message),(byte)2 );
        CraftPlayer p = (CraftPlayer) player;
        p.getHandle().playerConnection.sendPacket(packet);
    }
    public BlockFace getRelativeFace(Location location, Material material){
        Block block = location.getBlock();
        for(BlockFace face : BlockFace.values()){
            if(block.getRelative(face).getType() == material){
                return face;
            }
        }
        return null;
    }
    public void placebeddef(ITeam team){
        Random random = new Random();
        Material material = Material.WOOD;
        switch (random.nextInt(7)){
            case 1:
                material = Material.SANDSTONE;
                break;
            case 2:
                material = Material.ENDER_STONE;
                break;
            case 3:
                material = Material.STAINED_GLASS;
                break;
            case 4:
                material = Material.WOOL;
                break;
        }
        ArrayList<Location> layer1 = new ArrayList<>();
        ArrayList<Location> layer2 = new ArrayList<>();
        ArrayList<Location> layer3 = new ArrayList<>();
        Location loc1 = team.getBed().getBlock().getLocation();
        if(getRelativeFace(team.getBed().getBlock().getLocation(),Material.BED_BLOCK) == null){
            Bukkit.getLogger().warning("无法为"+ team.getName() + "守家 (loc2=null)");
            return;
        }
        Location loc2 = loc1.getBlock().getRelative(getRelativeFace(team.getBed().getBlock().getLocation(),Material.BED_BLOCK)).getLocation();
        if(loc2 == null){
            Bukkit.getLogger().warning("无法为"+ team.getName() + "守家 (loc2=null)");
            return;
        }
        List<Location> locs = new ArrayList<>();
        locs.add(loc1);
        locs.add(loc2);
        //逻辑： 4面环绕 反正不是空气的不会填充
        //借鉴自kinomc的RushBedwarsMode
        for(Location loc : locs){
            layer1.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()-1.0));
            layer1.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()+1.0));
            layer1.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY()+1.0,loc.getZ()));
            layer1.add(new Location(team.getArena().getWorld(),loc.getX()+1,loc.getY(),loc.getZ()));
            layer1.add(new Location(team.getArena().getWorld(),loc.getX()-1,loc.getY(),loc.getZ()));
        }
        for(Location loc : layer1){
            layer2.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()-1.0));
            layer2.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()+1.0));
            layer2.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY()+1.0,loc.getZ()));
            layer2.add(new Location(team.getArena().getWorld(),loc.getX()+1,loc.getY(),loc.getZ()));
            layer2.add(new Location(team.getArena().getWorld(),loc.getX()-1,loc.getY(),loc.getZ()));
        }
        for(Location loc : layer2){
            layer3.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()-1.0));
            layer3.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY(),loc.getZ()+1.0));
            layer3.add(new Location(team.getArena().getWorld(),loc.getX(),loc.getY()+1.0,loc.getZ()));
            layer3.add(new Location(team.getArena().getWorld(),loc.getX()+1,loc.getY(),loc.getZ()));
            layer3.add(new Location(team.getArena().getWorld(),loc.getX()-1,loc.getY(),loc.getZ()));
        }
        //Fill Layer1
        for(Location loc : layer1){
            if(team.getArena().getWorld().getBlockAt(loc).getType() == Material.AIR){
                Block block = loc.getBlock();
                block.setType(material);
                if(material == Material.STAINED_GLASS || material == Material.WOOL){
                    block.setData(team.getColor().itemByte());
                }
                team.getArena().addPlacedBlock(block);
            }
        }
        //Fill Layer2
        for(Location loc : layer2){
            if(team.getArena().getWorld().getBlockAt(loc).getType() == Material.AIR){
                Block block = loc.getBlock();
                block.setType(Material.WOOL);
                block.setData(team.getColor().itemByte());
                team.getArena().addPlacedBlock(block);
            }
        }
        //Fill Layer3
        for(Location loc : layer3){
            if(team.getArena().getWorld().getBlockAt(loc).getType() == Material.AIR){
                Block block = loc.getBlock();
                block.setType(Material.STAINED_GLASS);
                block.setData(team.getColor().itemByte());
                team.getArena().addPlacedBlock(block);
            }
        }
        //BED PROTECTION END
    }
}