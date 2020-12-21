INSERT INTO matching (apartmentId, userId, timestamp) VALUES (1, 1, "Ljubljana", "apt", "tbd");

@Column(name = "apartmentID")
    private int apartmentId;

    @Column(name = "userID")
    private int userId;

    @Column(name = "location")
    private String location;

    @Column(name = "initiator")
    private String initiator;

    @Column(name = "mutual")
    private String mutual;