package termProject;

import termProject.controller.*;
import termProject.embeddable.*;
import termProject.entity.*;
import termProject.type.DiscountType;
import termProject.type.FindType;
import termProject.type.HouseType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Controller implements Runnable {
    private final EntityManagerFactory emf;
    private final EntityManager em;

    private GuestController guestController;
    private HostController hostController;
    private HouseController houseController;
    private ReservationController reservationController;
    private ReviewController reviewController;

    public Controller(EntityManagerFactory emf) {
        this.emf = emf;
        this.em = this.emf.createEntityManager();
        this.getContollers(em);
    }

    public void getContollers(EntityManager em) {
        guestController = new GuestController(em);
        hostController = new HostController(em);
        houseController = new HouseController(em);
        reservationController = new ReservationController(em);
        reviewController = new ReviewController(em);
    }

    public
    int page = 1;
    int input;
    int month = new Date().getMonth() + 1;
    Scanner sc = new Scanner(System.in);

    @Override
    public void run() {
        LocalDate date = LocalDate.now();

        while (true) {
            switch (page) {
                case 1:
                    System.out.println();
                    System.out.println("  0. 더미 데이터 생성");
                    System.out.println("검사항목 1) 호스트는 숙소를 등록할 수 있다");
                    System.out.println("  1. 숙소 등록");
                    System.out.println("  9. 다음");
                    System.out.print("input: ");

                    input = sc.nextInt();
                    switch (input) {
                        case 0:
                            guestController.createSampleData();
                            hostController.createSampleData();
                            houseController.createSampleData();
                            reservationController.createSampleData();
                            reviewController.createSampleData();
                            continue;
                        case 1:
                            // 숙소 등록
                            String name = "세울 게스트하우스";
                            String description = "촬영 목적으로 이용하는 게스트 분들을 위해 다락방 컨셉의 퀸사이즈 토퍼2개, 담요, 쿠션을 제공해드리고 있습니다.";
                            HouseType houseType = HouseType.PERSONAL;
                            Address address = Address.create("서울시", " 종로 66", "34523");
                            AcceptanceInfo acceptanceInfo = AcceptanceInfo.create(2, 1, 4);
                            DiscountPolicy discountPolicy = DiscountPolicy.create(DiscountType.NONE, 0, 0);
                            Facility facility = Facility.create(0, 1, 2, 3);
                            Cost cost = Cost.create(23000, 25000);

                            houseController.registHouse(name, description, houseType, address, acceptanceInfo, discountPolicy, facility, cost);
                            continue;
                        case 9:
                            // 다음
                            page = 2;
                            break;
                        default:
                            System.out.println("Wrong Input");
                            continue;
                    }
                    break;

                case 2:
                    System.out.println();
                    System.out.println("검사항목 2) 호스트는 특정 기간에 정량 할인이나 정률 할인을 적용할 수 있다.");
                    System.out.println("  1. 할인 조회");
                    System.out.println("  2. 할인 적용 (정률)");
                    System.out.println("  3. 할인 적용 (정량)");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 할인 조회
                            houseController.calPrice(1L);
                            houseController.calPrice(2L);
                            continue;
                        case 2:
                            // 할인 적용
                            int value = 35;
                            DiscountType type = DiscountType.FIXED;
                            System.out.print("할인 방식(정률, 정량, 없음): ");
                            System.out.println("정률 " + value + "%");
                            houseController.applyDicountPolicy(1L, type, value);
                            continue;
                        case 3:
                            value = 12000;
                            type = DiscountType.VARIABLE;
                            System.out.print("할인 방식(정률, 정량, 없음): ");
                            System.out.println("정량 " + value + "원");
                            houseController.applyDicountPolicy(2L, type, value);
                            continue;
                        case 9:
                            // 다음
                            page = 3;
                            break;
                        case 0:
                            // 이전
                            page = 1;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 3:
                    System.out.println();
                    System.out.println("검사항목 3) 게스트는 조건에 맞는 숙소를 조회할 수 있다.");
                    System.out.println("  1. 체크인: 11.27, 체크아웃 11.30, 인원: 5, 숙소 유형: 전체");
                    System.out.println("  2. 체크인: 11.27, 체크아웃 11.30, 인원: 모두, 숙소 유형: 모두");
                    System.out.println("  3. 체크인: 11.27, 체크아웃 11.30, 인원: 7, 숙소 유형: 모두");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 숙소 조회
                            Date checkinDate1 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 27);
                            Date checkoutDate1 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 30);
                            int people1 = 5;
                            HouseType houseType1 = HouseType.WHOLE;

                            // 동적 쿼리1 (date, date, people, type)
                            List<House> houseList1 = houseController.findHouse(checkinDate1, checkoutDate1, people1, houseType1);
                            houseList1.forEach(v -> System.out.println(v +
                                    "인원: " + people1 + "명\n" +
                                    "총 금액: " + houseController.getTotalPrice(v, checkinDate1, checkoutDate1, people1) + "원" + "\n"));
                            continue;
                        case 2:
                            Date checkinDate2 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 27);
                            Date checkoutDate2 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 30);

                            // 동적 쿼리2 (date, date, null, null)
                            List<House> houseList2 = houseController.findHouse(checkinDate2, checkoutDate2, null, null);
                            houseList2.forEach(v -> System.out.println(v +
                                    "총 금액: " + houseController.getTotalPrice(v, checkinDate2, checkoutDate2) + "원" + "\n"));
                            continue;
                        case 3:
                            Date checkinDate3 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 27);
                            Date checkoutDate3 = new Date(date.getYear() - 1900, Calendar.NOVEMBER, 30);
                            int people3 = 7;

                            // 동적 쿼리2 (date, date, people, null)
                            List<House> houseList3 = houseController.findHouse(checkinDate3, checkoutDate3, people3, null);
                            houseList3.forEach(v -> System.out.println(v +
                                    "인원: " + people3 + "명\n" +
                                    "총 금액: " + houseController.getTotalPrice(v, checkinDate3, checkoutDate3, people3) + "원" + "\n"));
                            continue;
                        case 9:
                            // 다음
                            page = 4;
                            break;
                        case 0:
                            // 이전
                            page = 2;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 4:
                    System.out.println();
                    System.out.println("검사항목 4) 게스트는 선택한 숙소의 상세 정보를 조회할 수 있다.");
                    System.out.println("  1. 상세 조회");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 상세 조회
                            System.out.print("숙소 아이디 입력: ");
                            Long input = 1L;
                            System.out.println(input);
                            houseController.houseDetail(input, month);
                            continue;
                        case 9:
                            // 다음
                            page = 5;
                            break;
                        case 0:
                            // 이전
                            page = 3;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 5:
                    System.out.println();
                    System.out.println("검사항목 5) 체크인, 체크아웃 날짜와 인원을 입력하여 예약을 진행한다.");
                    System.out.println("  1. 상세 조회 (개인)");
                    System.out.println("  2. 숙소 예약 (개인)");
                    System.out.println("  3. 상세 조회 (전체)");
                    System.out.println("  4. 숙소 예약 (전체)");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            System.out.print("숙소 아이디 입력: ");
//                            Long input = sc.nextLong();
                            Long input = 2L;
                            System.out.println(input);
                            houseController.houseDetail(input, month);
                            continue;
                        case 2:
                            // 숙소 예약
                            System.out.print("게스트 아이디 입력: ");
//                            Long guestId = sc.nextLong();
                            Long guestId = 3L;
                            System.out.println(guestId);

                            System.out.print("숙소 아이디 입력: ");
//                            Long houseId = sc.nextLong();
                            Long houseId = 2L;
                            System.out.println(houseId);

                            System.out.println(month + "월 예약");
                            System.out.print("체크인 날짜 입력: ");
//                            int date1 = sc.nextInt();
                            int date1 = 28;
                            System.out.println(28);

                            System.out.print("체크아웃 날짜 입력: ");
//                            int date2 = sc.nextInt();
                            int date2 = 30;
                            System.out.println(30);

                            Date checkin = new Date(date.getYear() - 1900, Calendar.NOVEMBER, date1, 15, 0);
                            Date checkout = new Date(date.getYear() - 1900, Calendar.NOVEMBER, date2, 11, 0);
                            reservationController.bookHouse(guestId, houseId, checkin, checkout, 4);
                            continue;
                        case 3:
                            System.out.print("숙소 아이디 입력: ");
//                            Long input = sc.nextLong();
                            input = 3L;
                            System.out.println(input);
                            houseController.houseDetail(input, month);
                            continue;
                        case 4:
                            System.out.print("게스트 아이디 입력: ");
                            guestId = 3L;
                            System.out.println(guestId);
                            System.out.print("숙소 아이디 입력: ");
                            houseId = 3L;
                            System.out.println(houseId);
                            System.out.println(month + "월 예약");
                            System.out.print("체크인 날짜 입력: ");
                            date1 = 24;
                            System.out.println(date1);
                            System.out.print("체크아웃 날짜 입력: ");
                            date2 = 29;
                            System.out.println(date2);
                            checkin = new Date(date.getYear() - 1900, Calendar.NOVEMBER, date1, 15, 0);
                            checkout = new Date(date.getYear() - 1900, Calendar.NOVEMBER, date2, 11, 0);
                            reservationController.bookHouse(guestId, houseId, checkin, checkout, 4);
                            continue;
                        case 9:
                            // 다음
                            page = 6;
                            break;
                        case 0:
                            // 이전
                            page = 4;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 6:
                    System.out.println();
                    System.out.println("검사항목 6) 게스트는 예약한 숙소를 취소할 수 있다.");
                    System.out.println("  1. 예약 조회");
                    System.out.println("  2. 날짜 조회");
                    System.out.println("  3. 예약 취소");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 예약 조회
                            System.out.print("게스트 아이디 입력: ");
//                            Long guestId = sc.nextLong();
                            Long guestId = 3L;
                            System.out.println(guestId);
                            guestController.reservationHistory(guestId, FindType.ONCOMING);
                            continue;
                        case 2:
                            // 날짜 조회
                            System.out.print("숙소 아이디 입력: ");
//                            Long input = sc.nextLong();
                            Long houseId = 3L;
                            System.out.println(houseId);
                            houseController.houseDetail(houseId, month);
                            continue;
                        case 3:
                            // 예약 취소
                            System.out.print("예약 번호 입력: ");
                            Long reserveId = sc.nextLong();
                            reservationController.cancelReserve(reserveId);
                            continue;
                        case 9:
                            // 다음
                            page = 7;
                            break;
                        case 0:
                            // 이전
                            page = 5;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 7:
                    System.out.println();
                    System.out.println("검사항목 7) 마이페이지");
                    System.out.println("  1. 전체");
                    System.out.println("  2. 체크인 예정인 숙소");
                    System.out.println("  3. 체크아웃이 완료된 숙소");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        // ReservationHistory(guestId, findType): findType  all, oncoming, terminated
                        case 1:
                            System.out.print("게스트 아이디 입력: ");
//                            Long guestId = sc.nextLong();
                            Long guestId = 1L;
                            System.out.println(guestId);
                            guestController.reservationHistory(guestId, FindType.ALL);
                            continue;
                        case 2:
                            System.out.print("게스트 아이디 입력: ");
//                            guestId = sc.nextLong();
                            guestId = 1L;
                            System.out.println(guestId);
                            guestController.reservationHistory(guestId, FindType.ONCOMING);
                            continue;
                        case 3:
                            System.out.print("게스트 아이디 입력: ");
//                            guestId = sc.nextLong();
                            guestId = 1L;
                            System.out.println(guestId);
                            guestController.reservationHistory(guestId, FindType.TERMINATED);
                            continue;
                        case 9:
                            // 다음
                            page = 8;
                            break;
                        case 0:
                            // 이전
                            page = 6;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 8:
                    System.out.println();
                    System.out.println("검사항목 8) 게스트는 체크아웃이 완료된 숙소에 별점(1~5)과 후기를 작성할 수 있다.");
                    System.out.println("  1. 후기 등록");
                    System.out.println("  2. 상세 조회");
                    System.out.println("  9. 다음");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 후기 등록
                            System.out.print("게스트 아이디 입력: ");
//                            Long guestId = sc.nextLong();
                            Long guestId = 1L;
                            System.out.println(guestId);

                            System.out.print("예약 번호 입력: ");
                            Long reserveId = sc.nextLong();

                            System.out.print("별점 입력: ");
//                            int star = sc.nextInt();
                            int star = 5;
                            System.out.println(star);

                            String comment = "정말 깨끗해요!";
                            System.out.println("코멘트: " + comment);

                            reviewController.addComment(guestId, reserveId, star, comment);
                            continue;
                        case 2:
                            System.out.print("숙소 아이디 입력: ");
//                            Long houseId = sc.nextLong();
                            Long houseId = 2L;
                            System.out.println(houseId);
                            houseController.houseDetail(houseId, month);
                            continue;
                        case 9:
                            // 다음
                            page = 9;
                            break;
                        case 0:
                            // 이전
                            page = 7;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;

                case 9:
                    System.out.println();
                    System.out.println("검사항목 9) 호스트는 지정한 달의 매출을 확인할 수 있다.");
                    System.out.println("  1. 예약 현황");
                    System.out.println("  2. 수익 조회");
                    System.out.println("  9. 종료");
                    System.out.println("  0. 이전");
                    System.out.print("input: ");
                    input = sc.nextInt();
                    switch (input) {
                        case 1:
                            // 예약 현황
//                            System.out.print("호스트 아이디 입력: ");
//                            Long hostId = sc.nextLong();
                            hostController.showReserveState(1L, month);
                            hostController.showReserveState(2L, month);
                            continue;
                        case 2:
                            // 수익 조회
//                            System.out.print("호스트 아이디 입력: ");
//                            hostId = sc.nextLong();
                            System.out.println(hostController.calRevenue(1L, month));
                            System.out.println(hostController.calRevenue(2L, month));
                            continue;
                        case 9:
                            // 종료
                            page = 10;
                            break;
                        case 0:
                            // 이전
                            page = 8;
                            break;
                        default:
                            System.out.println("Wrong Input");
                    }
                    break;
                case 10:
                    System.out.println();
                    System.out.println("종료");
                    emf.close();
                    System.exit(0);
            }

        }
    }


}
