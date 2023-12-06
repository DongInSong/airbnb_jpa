package termProject.controller;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import termProject.Calender;
import termProject.type.DiscountType;
import termProject.type.HouseType;
import termProject.embeddable.*;
import termProject.entity.Host;
import termProject.entity.House;
import termProject.entity.QHouse;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HouseController {
    private final EntityManager em;
    private final EntityTransaction tx;
    private final JPAQueryFactory jpaQueryFactory;
    private final Calender calender;

    public HouseController(EntityManager em) {
        this.em = em;
        this.tx = em.getTransaction();
        this.jpaQueryFactory = new JPAQueryFactory(em);
        calender = new Calender();
    }

    // 호스트 여부 확인하는 함수
    public Host isValidHost() {
        Scanner sc = new Scanner(System.in);
        Host host;
        while (true) {
            System.out.println("호스트 아이디 입력: ");
            Long host_id = sc.nextLong();
            host = em.find(Host.class, host_id);
            if (host != null) {
                if (host.getHouse() == null) {
                    return host;
                } else System.out.println("이미 숙소가 등록된 호스트입니다.");
            } else System.out.println("존재하지 않는 아이디입니다.");
        }
    }

    // 입력한 호스트 아이디에 대한 호스트를 반환하는 함수
    public Host getHost() {
        Scanner sc = new Scanner(System.in);
        Host host;
        while (true) {
            System.out.println("호스트 아이디 입력: ");
            Long host_id = sc.nextLong();
            host = em.find(Host.class, host_id);
            if (host != null) {
                if (host.getHouse() != null) {
                    return host;
                } else System.out.println("숙소가 등록되지 않은 호스트입니다.");
            } else System.out.println("존재하지 않는 아이디입니다.");
        }
    }

    // 숙소를 등록하는 함수
    public void registHouse(String name, String description, HouseType type, Address address, AcceptanceInfo acceptanceInfo, DiscountPolicy discountPolicy, Facility facility, Cost cost) {
        try {
            tx.begin();
//            Host host = isValidHost();
            Host host = em.find(Host.class, 4L);
            House house = House.create(name, address, host, description, type, acceptanceInfo, discountPolicy, facility, cost);
            em.persist(house);
            host.setHouse(house);

            System.out.println(host);
            System.out.println(house);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

    // 할인 정책을 적용하는 함수
    public void applyDicountPolicy(Long hostId, DiscountType type, int value) {
        try {
            tx.begin();
            Host host = em.find(Host.class, hostId);
            House house = em.find(House.class, host.getHouse().getId());
            house.applyDiscountPolicy(type, value);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

    // 금액을 출력하는 함수
    public void calPrice(Long hostId) {
        try {
//            Host host = getHost();
            Host host = em.find(Host.class, hostId);
            TypedQuery<House> query = em.createQuery("SELECT h FROM House h WHERE h.host = :host", House.class);
            List<House> result = query.setParameter("host", host).getResultList();

            for (House house : result) {
                System.out.println(house.printCost());
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

    // 총 금액을 반환하는 함수
    public int getTotalPrice(House house, Date checkin, Date checkout) {
        LocalDate checkin_localDate = LocalDate.ofInstant(checkin.toInstant(), ZoneId.systemDefault());
        LocalDate checkout_localDate = LocalDate.ofInstant(checkout.toInstant(), ZoneId.systemDefault());
        int days = (int) ChronoUnit.DAYS.between(checkin_localDate, checkout_localDate) + 1;
        return house.getCost().getWeekdays_discount() * days;
    }

    // 총 금액을 반환하는데 인원 수까지 계산하여 반환하는 함수
    public int getTotalPrice(House house, Date checkin, Date checkout, int people) {
        if(house.getHouseType().equals(HouseType.PERSONAL)) {
            return getTotalPrice(house, checkin, checkout) * people;
        }
        return getTotalPrice(house, checkin, checkout);
    }

    public void houseDetail(Long houseId, int month) {
        try {
            House house = em.find(House.class, houseId);
            if (house == null) {
                System.out.println("잘못된 숙소 아이디입니다.");
                return;
            }

            System.out.println(house);
            if (house.getReviewList() != null)
                house.getReviewList().forEach(System.out::println);
            if (house.getReviewList() != null)
                house.getReservationList().forEach(System.out::println);
            calender.getCalender(month, house);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

    // DSL과 booleanExpression을 사용
    public List<House> findHouse(Date checkin, Date checkout, Integer people, HouseType houseType) {
        QHouse qHouse = QHouse.house;
        return jpaQueryFactory.selectFrom(qHouse).orderBy(qHouse.cost.weekdays_discount.asc()).
                where(gtRoom(people),
                        eqType(houseType),
                        btwCheckin(checkin, checkout),
                        btwCheckout(checkin, checkout)).fetch();
    }

    private static BooleanExpression gtRoom(Integer room) {
        if (room == null) {
            return null;
        }
        return QHouse.house.acceptanceInfo.people.goe(room);
    }

    private static BooleanExpression eqType(HouseType houseType) {
        if (houseType == null) {
            return null;
        }
        return QHouse.house.houseType.eq(houseType);
    }

    private static BooleanExpression btwCheckin(Date checkin, Date checkout) {
        if (checkin == null || checkout == null) {
            return null;
        }
        return QHouse.house.reservationList.any().checkin.between(checkin, checkout).not();
    }

    private static BooleanExpression btwCheckout(Date checkin, Date checkout) {
        if (checkin == null || checkout == null) {
            return null;
        }
        return QHouse.house.reservationList.any().checkout.between(checkin, checkout).not();
    }

    // 샘플 데이터 생성
    public void createSampleData() {
        try {
            tx.begin();
            String name = "귤밭속 독채풀빌라";
            String description = "해당 지역에서 수영장을 갖춘 몇 안 되는 숙소 중 하나입니다.";
            HouseType houseType = HouseType.WHOLE;
            Address address = Address.create("서귀포시", "성산읍", "23412");
            AcceptanceInfo acceptanceInfo = AcceptanceInfo.create(2, 2, 6);
            Cost cost = Cost.create(320000, 350000);
            DiscountPolicy discountPolicy = DiscountPolicy.create(DiscountType.NONE, 0, 0);
            Facility facility = Facility.create(2, 4, 1, 2);
            Host host = em.find(Host.class, 1L);

            House house = House.create(name, address, host, description, houseType, acceptanceInfo, discountPolicy, facility, cost);
            host.setHouse(house);
            em.persist(house);

            name = "양평 독채풀빌라 스테이호은";
            description = "양평의 한적한 시골마을에 위치한 자연친화적 독채 스테이입니다. ";
            houseType = HouseType.PERSONAL;
            address = Address.create("양평군", "", "45231");
            acceptanceInfo = AcceptanceInfo.create(3, 2, 6);
            cost = Cost.create(45200, 47000);
            discountPolicy = DiscountPolicy.create(DiscountType.NONE, 0, 0);
            facility = Facility.create(4, 5, 1, 0);
            host = em.find(Host.class, 2L);

            house = House.create(name, address, host, description, houseType, acceptanceInfo, discountPolicy, facility, cost);
            host.setHouse(house);
            em.persist(house);

            name = "오로지 한팀을 위한 팜스테이 선셋파머스";
            description = "어재리(금산) 깊숙한 산골짜기 아래 고요하게 자리잡은 노을아래 흙과 나무 자연이 어울려 만들어진  팜스테이입니다.";
            houseType = HouseType.WHOLE;
            address = Address.create("금산군", "부리면", "34212");
            acceptanceInfo = AcceptanceInfo.create(3, 2, 8);
            cost = Cost.create(190000, 210000);
            discountPolicy = DiscountPolicy.create(DiscountType.NONE, 0, 0);
            facility = Facility.create(0, 3, 0, 4);
            host = em.find(Host.class, 3L);

            house = House.create(name, address, host, description, houseType, acceptanceInfo, discountPolicy, facility, cost);
            host.setHouse(house);
            em.persist(house);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        } finally {
            em.clear();
        }
    }

}
