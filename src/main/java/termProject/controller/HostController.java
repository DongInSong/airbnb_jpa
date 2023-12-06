package termProject.controller;

import termProject.type.HouseType;
import termProject.embeddable.Address;
import termProject.entity.Host;
import termProject.entity.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class HostController {
    private final EntityManager em;
    private final EntityTransaction tx;

    public HostController(EntityManager em) {
        this.em = em;
        this.tx = em.getTransaction();
    }

    // 호스트 기준 예약 현황을 출력하는 함수
    public void showReserveState(Long hostId, int month) {
        Host host = em.find(Host.class, hostId);
        if (host.getHouse() == null) {
            return;
        }
        List<Reservation> allReservation = host.getHouse().getReservationList();
        allReservation.sort(Comparator.comparing(Reservation::getCheckin));
        System.out.println();
        System.out.println("[호스트 정보]");
        System.out.println(host);
        System.out.println("[" + month + "월 예약 현황]");
        System.out.printf("%-30s %-9s %-8s %10s  %5s\n", "숙소명", "체크인", "체크아웃", "요금", "결제");
        allReservation.forEach(v -> {
            System.out.printf("%-25s %ty.%<tm.%<-5td %ty.%<tm.%<-5td %10s원 %5s\n", v.getHouse().getName(), v.getCheckin(), v.getCheckout(), getTotalCost(v.getId()), isCheckedOut(v) ? "o" : "x");
        });
    }

    public String calRevenue(Long hostId, int month) {
        Host host = em.find(Host.class, hostId);
        if (host.getHouse() == null) {
            return null;
        }
        List<Reservation> allReservation = host.getHouse().getReservationList();
        allReservation.sort(Comparator.comparing(Reservation::getCheckin));
        LocalDate checkout_localDate = null;
        int revenue = 0;
        for (Reservation reservation : allReservation) {
            checkout_localDate = LocalDate.ofInstant(reservation.getCheckout().toInstant(), ZoneId.systemDefault());
            if (LocalDate.now().isAfter(checkout_localDate)) {
                revenue += getTotalCost(reservation.getId());
            }
        }
        return "\n" +
                "[" + month + "월 매출]" + "\n" +
                revenue + "원";
    }

    // 체크아웃 여부(체크아웃 날짜가 현재 날짜를 지나면)를 확인하는 함수
    private boolean isCheckedOut(Reservation reservation) {
        LocalDate checkout_localDate = null;
        checkout_localDate = LocalDate.ofInstant(reservation.getCheckout().toInstant(), ZoneId.systemDefault());
        if (LocalDate.now().isAfter(checkout_localDate)) {
            return true;
        }
        return false;
    }

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

    // 샘플 데이터 생성
    public void createSampleData() {
        try {
            tx.begin();
            Address address1 = Address.create("대구시", "남산대로 42", "41234");
            Address address2 = Address.create("서울시", "세종대로 23", "45231");
            Address address3 = Address.create("구미시", "대학로 17", "42331");
            Address address4 = Address.create("부산시", "부산로 22", "25347");
            Host host1 = Host.create("송동인", address1);
            Host host2 = Host.create("김서울", address2);
            Host host3 = Host.create("안구미", address3);
            Host host4 = Host.create("구부산", address4);
            em.persist(host1);
            em.persist(host2);
            em.persist(host3);
            em.persist(host4);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }
}
