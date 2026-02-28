//package com.example.WingPulse.model.dto.request;
//
//import com.example.WingPulse.model.User;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import lombok.Data;
//
//@Data
//public class TeamRequest {
//
//    @JsonProperty("name")
//    private String name;
//
//    @JsonProperty("manager_id")
//    private User manager;
//
//}
////  @Id
////     @GeneratedValue(strategy = GenerationType.IDENTITY)
////     private Long id;
//
////     @Column(nullable = false)
////     private String name;
//
////     @OneToOne
////     @JoinColumn(name = "manager_id")
////     private User manager;
//
//
//
////     @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
////     private List<User> members = new ArrayList<>();