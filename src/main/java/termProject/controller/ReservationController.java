package termProject.controller;

import termProject.type.HouseType;
import termProject.entity.Guest;
import termProject.entity.House;
import termProject.entity.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReservationController {
    private final EntityManager em;
    private final EntityTransaction tx;

    public ReservationController(EntityManager em) {
        this.em = em;
        this.tx = em.getTransaction();
    }


    // 게스트 아이디, 숙소 아이디를 받아 예약을 실행하는 함수
    public void bookHouse(Long guestId, Long houseId, Date checkin, Date checkout, int numOfPeople) {
        Guest guest = em.find(Guest.class, guestId);
        House house = em.find(House.class, houseId);
        if (guest == null || house == null) {
            System.out.println("잘못된 입력입니다.");
            return;
        }
        if (house.getHouseType().equals(HouseType.PERSONAL)) {
            personalBooking(guest, house, checkin, checkout, numOfPeople);
        }

        if (house.getHouseType().equals(HouseType.WHOLE)) {
            wholeBooking(guest, house, checkin, checkout, numOfPeople);
        }
    }

    // 예약을 취소하는 함수
    public void cancelReserve(Long reserveId) {
        Reservation reservation = em.find(Reservation.class, reserveId);
        if (reservation == null) {
            return;
        }
        if (!isCheckedIn(reservation.getCheckin())) {
            System.out.println("취소가 불가능합니다.");
            return;
        }
        try {
            tx.begin();
            em.remove(reservation);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

    private boolean isCheckedIn(Date checkin) {
        LocalDate localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
        if (LocalDate.now().isBefore(localDate)) {
            return true;
        }
        return false;
    }

    // 숙소 유형 개인에 대한 예약을 실행하는 함수
    private void personalBooking(Guest guest, House house, Date checkin, Date checkout, int numOfPeople) {
        List<LocalDate> bookedDates = getBookedDates(house);
        if (bookedDates == null) {
            executeBooking(guest, house, checkin, checkout, numOfPeople);
        } else {
            Map<LocalDate, Long> dateCountMap = countDates(bookedDates);
            int currentRoom = house.getAcceptanceInfo().getRoom();

            LocalDate checkin_localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
            LocalDate checkout_localDate = LocalDate.ofInstant(checkout.toInstant(), ZoneId.systemDefault());
            while (!checkin_localDate.isAfter(checkout_localDate)) {
                for (Map.Entry<LocalDate, Long> entry : dateCountMap.entrySet()) {
                    if (checkin_localDate.equals(entry.getKey())) {
                        if (currentRoom - entry.getValue() == 0) {
                            System.out.println("방이 부족합니다.");
                            return;
                        }
                    }
                }
                checkin_localDate = checkin_localDate.plusDays(1);
            }
            executeBooking(guest, house, checkin, checkout, numOfPeople);
        }
    }

    // 숙소 유형 전체에 대한 예약을 실행하는 함수
    private void wholeBooking(Guest guest, House house, Date checkin, Date checkout, int numOfPeople) {
        List<LocalDate> bookedDates = getBookedDates(house);
        if (bookedDates == null) {
            executeBooking(guest, house, checkin, checkout, numOfPeople);
        } else if (!checkRoom(checkin, checkout, bookedDates)) {
            System.out.println("날짜가 중복됩니다.");
        } else executeBooking(guest, house, checkin, checkout, numOfPeople);

    }

    // 예약을 실제 데이테베이스에 삽입하는 함수
    private void executeBooking(Guest guest, House house, Date checkin, Date checkout, int numOfPeople) {
        try {
            tx.begin();
            Reservation reservation = Reservation.create(checkin, checkout, numOfPeople);
            reservation.setReservation(guest, house);
            em.persist(reservation);
            tx.commit();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            em.clear();
        }
    }


    // 예약된 날짜 리스트와 중복되는 날이 있는지 체크하는 함수
    private boolean checkRoom(Date checkin, Date checkout, List<LocalDate> bookedDates) {
        LocalDate checkin_localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
        LocalDate checkout_localDate = LocalDate.ofInstant(checkout.toInstant(), ZoneId.systemDefault());
        return !bookedDates.contains(checkin_localDate) && !bookedDates.contains(checkout_localDate);
    }

    // 예약 된 날짜에서 같은 날짜를 카운트하여 맵핑 후 맵을 리턴하는 함수
    private Map<LocalDate, Long> countDates(List<LocalDate> dates) {
        Map<LocalDate, Long> dateCountMap = dates.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        dateCountMap = dateCountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return dateCountMap;
    }

    // 해당되는 숙소에 대한 모든 예약 날짜를 리스트로 반환하는 함수
    private List<LocalDate> getBookedDates(House house) {
        if (house.getReservationList().isEmpty()) {
            return null;
        }

        Date checkin = null;
        Date checkout = null;
        List<LocalDate> totalDates = new ArrayList<>();
        for (int i = 0; i < house.getReservationList().size(); i++) {
            checkin = house.getReservationList().get(i).getCheckin();
            checkout = house.getReservationList().get(i).getCheckout();

            LocalDate checkin_localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
            LocalDate checkout_localDate = LocalDate.ofInstant(checkout.toInstant(), ZoneId.systemDefault());
            while (!checkin_localDate.isAfter(checkout_localDate)) {
                totalDates.add(checkin_localDate);
                checkin_localDate = checkin_localDate.plusDays(1);
            }
        }
        Collections.sort(totalDates);
        return totalDates;
    }

    // 샘플 데이터 생성
    public void createSampleData() {
        try {
            tx.begin();
            LocalDate date = LocalDate.now();
            // ONCOMING 시연용 1
            Date checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 28, 15, 0);
            Date checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 29, 11, 0);
            Reservation reservation = Reservation.create(checkinDate, checkoutDate, 1);
            Guest guest = em.find(Guest.class, 1L);
            House house = em.find(House.class, 1L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 21, 15, 0);
            checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 24, 11, 0);
            reservation = Reservation.create(checkinDate, checkoutDate, 4);
            guest = em.find(Guest.class, 2L);
            house = em.find(House.class, 1L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 5, 15, 0);
            checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 9, 11, 0);
            reservation = Reservation.create(checkinDate, checkoutDate, 4);
            guest = em.find(Guest.class, 4L);
            house = em.find(House.class, 1L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            // 리뷰 시연용
            checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 22, 15, 0);
            checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 24, 11, 0);
            reservation = Reservation.create(checkinDate, checkoutDate, 2);
            guest = em.find(Guest.class, 1L);
            house = em.find(House.class, 2L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 20, 15, 0);
            checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 23, 11, 0);
            reservation = Reservation.create(checkinDate, checkoutDate, 3);
            guest = em.find(Guest.class, 2L);
            house = em.find(House.class, 2L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            checkinDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 23, 15, 0);
            checkoutDate = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 26, 11, 0);
            reservation = Reservation.create(checkinDate, checkoutDate, 3);
            guest = em.find(Guest.class, 3L);
            house = em.find(House.class, 2L);
            reservation.setReservation(guest, house);
            em.persist(reservation);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }
}
