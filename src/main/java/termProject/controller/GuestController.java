package termProject.controller;

import termProject.type.FindType;
import termProject.type.HouseType;
import termProject.embeddable.Address;
import termProject.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GuestController {
    private final EntityManager em;
    private final EntityTransaction tx;

    public GuestController(EntityManager em) {
        this.em = em;
        this.tx = em.getTransaction();
    }

    // 게스트 예약 내역을 출력하는 함수
    public void reservationHistory(Long guestId, FindType findType) {
        Guest guest = em.find(Guest.class, guestId);
        if (guest == null) {
            return;
        }
        LocalDate checkin_localDate = null;
        LocalDate checkout_localDate = null;
        String title = null;
        List<Reservation> printList = new ArrayList<>();

        List<Reservation> allReservation = guest.getReservationList();

        if (allReservation.isEmpty()) {
            title = "숙박 내역이 없습니다.";
        } else {
            allReservation.sort(Comparator.comparing(Reservation::getCheckin));
            switch (findType) {
                case ALL:
                    title = "[모든 리스트]";
                    printList.addAll(allReservation);
                    break;
                case ONCOMING:
                    title = "[예약 리스트]";
                    for (Reservation reservation : allReservation) {
                        checkin_localDate = LocalDate.ofInstant(reservation.getCheckin().toInstant(), ZoneId.systemDefault());
                        if (LocalDate.now().isBefore(checkin_localDate) || LocalDate.now().isEqual(checkin_localDate)) {
                            printList.add(reservation);
                        }
                    }
                    break;
                case TERMINATED:
                    title = "[숙박 완료 리스트]";
                    for (Reservation reservation : allReservation) {
                        checkout_localDate = LocalDate.ofInstant(reservation.getCheckout().toInstant(), ZoneId.systemDefault());
                        if (LocalDate.now().isAfter(checkout_localDate) || LocalDate.now().isEqual(checkout_localDate)) {
                            printList.add(reservation);
                        }
                    }
                    break;
            }
            System.out.println();
            System.out.println(title);
            System.out.printf("%-7s %-30s %-9s %-8s %10s %5s\n", "예약번호", "숙소명", "체크인", "체크아웃", "요금", "후기");
            printList.forEach(v -> {
                System.out.printf("%-9d %-25s %ty.%<tm.%<-5td %ty.%<tm.%<-5td %10s원 %5s\n", v.getId(), v.getHouse().getName(), v.getCheckin(), v.getCheckout(), getTotalCost(v.getId()), checkReview(guest.getId(), v.getId()) ? "o" : "x");
            });
        }
    }

    // 결제 총 금액을 반환하는 함수
    private int getTotalCost(Long reserveId) {
        Reservation reservation = em.find(Reservation.class, reserveId);
        if (reservation == null) {
            return 0;
        }
        int weekdaysCost = reservation.getHouse().getCost().getWeekdays_discount();
        int numOfPeople = reservation.getNumOfPeople();
        Date checkin = reservation.getCheckin();
        Date checkout = reservation.getCheckout();

        LocalDate checkin_localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
        LocalDate checkout_localDate = LocalDate.ofInstant(checkout.toInstant(), ZoneId.systemDefault());
        int days = (int) ChronoUnit.DAYS.between(checkin_localDate, checkout_localDate) + 1;

        if (reservation.getHouse().getHouseType().equals(HouseType.WHOLE)) {
            return weekdaysCost * days;
        } else if (reservation.getHouse().getHouseType().equals(HouseType.PERSONAL)) {
            return weekdaysCost * days * numOfPeople;
        } else return 0;
    }

    // 게스트의 리뷰 작성 여부를 반환하는 함수
    private boolean checkReview(Long guestId, Long reserveId) {
        Guest guest = em.find(Guest.class, guestId);
        if (guest == null) {
            return false;
        }
        for (Review review : guest.getReviewList()) {
            if (Objects.equals(review.getReservation().getId(), reserveId)) {
                return true;
            }
        }
        return false;
    }

    // 샘플 데이터 생성
    public void createSampleData() {
        try {
            tx.begin();
            Address address1 = Address.create("대구시", "중구 115", "41234");
            Address address2 = Address.create("대구시", "남구 32", "45231");
            Address address3 = Address.create("구미시", "대학로 63", "42331");
            Address address4 = Address.create("서울시", "부산로 21", "25347");
            Guest guest1 = Guest.create("게스트1", address1);
            Guest guest2 = Guest.create("게스트2", address2);
            Guest guest3 = Guest.create("게스트3", address3);
            Guest guest4 = Guest.create("게스트4", address4);
            em.persist(guest1);
            em.persist(guest2);
            em.persist(guest3);
            em.persist(guest4);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }
}
