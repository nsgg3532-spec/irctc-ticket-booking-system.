package org.example.services;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {
    private User user;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();


    private static final String USERS_PATH ="app/src/main/java/org/example/localdb/users.json";

    public UserBookingService(User user) throws IOException {
        this.user=user;
//        File users = new File(USERS_PATH);
//        userList=objectMapper.readValue(users,new TypeReference<List<User>>(){});
        loadUserListFromFile();
    }
    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException{
        userList=objectMapper.readValue(new File(USERS_PATH), new TypeReference<List<User>>(){});
    }
    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
           return user1.getName().equals(user.getName())&&UserServiceUtil.checkPassword(user.getPassword(),user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }
    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return  Boolean.TRUE;
        } catch (IOException e) {
            return Boolean.FALSE;
        }

    }
    private void saveUserListToFile() throws IOException{
        File userFile = new File(USERS_PATH);
        objectMapper.writeValue(userFile,userList);
    }

    public void fetchBookings(){
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTicket();
        }
    }

    public Boolean cancelBooking(String ticketId) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter The Ticket Id to cancel");
        ticketId = s.next();
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket id can not be null or empty");
            return Boolean.FALSE;
        }

        String finalTicketId1 = ticketId; // because strings are immutable
        boolean removed = user.getTicketBooked().removeIf(Ticket -> Ticket.getTicketId().equals(finalTicketId1));
        if (removed) {
            System.out.println("Ticket with ID" + ticketId + "has been canceled.");
            return Boolean.TRUE;
        }else {
            System.out.println("No ticket found with Id"+ticketId);
            return Boolean.FALSE;
        }
    }
    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService= new TrainService();
            return  trainService.searchTrains(source, destination);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }
    public Boolean bookTrainSeat(Train train, int row, int seat){
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats =train.getSeats();
            if(row>=0 && row<seats.size() && seat>=0 && seat < seats.get(row).size()){
                if(seats.get(row).get(seat)==0){
                    seats.get(row).set(seat,1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // booking successful
                } else {
                    return false; // seat is already booked
                }
            }else {
                return  false; //Invalid row or seat index
            }
        } catch (IOException e){
            return Boolean.FALSE;
        }
    }
}
