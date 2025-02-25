package edu.fje.daw2.unojava;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

@Path("/uno-java")
public class Api {
    @Context
    private UriInfo context;
    public static ArrayList<Partida> partidas = new ArrayList<>();
    public static ArrayList<Carta> cartas = new ArrayList<>();

    public Api() {

        for (int index = 0; index <= 9; index++) {
            cartas.add( new Carta(index , "vermell"));
            cartas.add(new Carta(index ,"blau"));
            cartas.add(new Carta(index ,"groc"));
            cartas.add(new Carta(index ,"verd"));
        }

    }
    @Path("/iniciarJoc/codiPartida")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    public Response crearPartida(@FormParam("idPartida") int id) {


        Partida partida = new Partida(id);
        if(partidas.contains(partida)) return Response.status(200).entity("La partida ja existeix. Uneix-t'hi \n"+partida.toString()).build();
        else{
            partidas.add(partida);
            for (Carta cart: cartas
                 ) {
                partida.afegirCarta(cart);
                cart.afegirPartida(partida);
            }

            return Response.status(200).entity(partidas.toString()).build();
        }


    }
    @Path("/unirse")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    public Response unirsePartida(@FormParam("idPartida") int id,@FormParam("nombre") int id_jugador) {


        Partida partida_temp = new Partida(id);


        if(!partidas.contains(partida_temp)) return Response.status(200).entity("La partida no existeix").build();



        int posicio = partidas.indexOf(partida_temp);
        Jugador jugador_temp = new Jugador(id_jugador,id_jugador+"");

        partida_temp = partidas.get(posicio);
        if(partida_temp.jugadores.contains(jugador_temp)) return Response.status(200).entity("El jugador esta registrat ja en la partida").build();

        jugador_temp.afegirPartida(partida_temp);
        partida_temp.afegirJugador(jugador_temp);

        ComprobarEstado(partida_temp,jugador_temp);
        return Response.status(200).entity("Jugador:"+jugador_temp.toString()).build();

    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/obtenirCarta/{jugador}")
    public String ObtenerCarta(@PathParam("jugador") String id_jugador) {
        for (Partida partida: partidas
        ) {
            int id_jugador_temp = Integer.parseInt(id_jugador);
            if(!partida.jugadores.contains(new Jugador(id_jugador_temp))) continue;

            Jugador jugador_temp = partida.jugadores.get(partida.jugadores.indexOf(new Jugador(id_jugador_temp)));

            int random = (int) ((Math.random() * (partida.cartas.size() - 0)) + 0);

            Carta carta_temp = partida.cartas.get(random);
            jugador_temp.afegirCarta(carta_temp);
            partida.eliminarCarta(carta_temp);

            carta_temp.afegirJugador(jugador_temp);

            ComprobarEstado(partida,jugador_temp);
            return "Jugador:"+jugador_temp.toString()+"\n"+carta_temp.toString();
        }
        return "Jugador no trobat";

    }

    @Path("/tirarCarta")
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    public Response unirsePartida(@FormParam("jugador") String id_jugador, @FormParam("numcarta") String num, @FormParam("colorcarta") String color) {
        for (Partida partida: partidas){

            int id_jugador_temp = Integer.parseInt(id_jugador);
            int num_temp = Integer.parseInt(num);
            String color_temp = color;

            if(!partida.jugadores.contains(new Jugador(id_jugador_temp))) continue;
            Jugador jugador_temp = partida.jugadores.get(partida.jugadores.indexOf(new Jugador(id_jugador_temp)));

            if (!jugador_temp.cartasJugador.contains(new Carta(num_temp,color_temp))) return Response.status(404).entity("Carta no trobada").build();
            Carta carta_llencar = jugador_temp.cartasJugador.get(jugador_temp.cartasJugador.indexOf(new Carta(num_temp, color_temp)));

            partida.afegirPila(carta_llencar);
            carta_llencar.eliminarJugador(jugador_temp);
            jugador_temp.eliminarCarta(carta_llencar);

            ComprobarEstado(partida,jugador_temp);
            return Response.status(200).entity("Carta a llençar: "+carta_llencar.toString()).build();
        }
        return Response.status(404).entity("Jugador no trobat").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/comprobarPila/{codiPartida}")
    public String comprobarPila(@PathParam("codiPartida") int id) {

        Partida partida_temp = new Partida(id);
        if (!partidas.contains(partida_temp)) return ("La partida no existeix");
        int posicio = partidas.indexOf(partida_temp);

        return (partidas.get(posicio).getPila().toString());

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/comprobarCartas/{jugador}")
    public String comprobarCartas(@PathParam("jugador") String id_jugador) {
        for (Partida partida: partidas){
            int id_jugador_temp = Integer.parseInt(id_jugador);

            if(!partida.jugadores.contains(new Jugador(id_jugador_temp))) continue;
            Jugador jugador_temp = partida.jugadores.get(partida.jugadores.indexOf(new Jugador(id_jugador_temp)));

            return (jugador_temp.cartasJugador.toString());
        }
        return ("Jugador no trobat");
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/acabarJoc/{idPartida}")
    public String acabarJoc(@PathParam("idPartida") String id_partida) {
        for (Partida partida: partidas){
            if(partida.id==Integer.parseInt(id_partida)){
                partidas.remove(partida);
            }
            return ("Partida esborrada");
        }
        return ("Partida no trobada");
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    @Path("/menu")
    public InputStream menu()
    {
        InputStream menu = (getClass().getClassLoader().getResourceAsStream("menu.html"));
        return menu;
    }

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    @Path("/menujs")
    public InputStream menujs()
    {
        InputStream menu = (getClass().getClassLoader().getResourceAsStream("menu.js"));
        return menu;
    }

    @GET
    @Path("/hello-world")
    @Produces("text/plain")
    public String hello() {
        return "Hello, World!";
    }

    public void ComprobarEstado(Partida partida_temp, Jugador jugador_temp){
        System.out.println(partida_temp);

    }
}