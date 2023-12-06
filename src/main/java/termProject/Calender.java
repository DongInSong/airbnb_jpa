package termProject;

import termProject.entity.House;
import termProject.type.HouseType;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Main class 
public class Calender {

    public Calender() {
    }

    public void getCalender(Integer mm, House house) {
        LocalDate date = LocalDate.now();

        int yy = date.getYear();
        int d = 1;
        int m = 1;
        int y = 1;
        int dy = 1;

        String day[] = {"SUN", "MON", "TUE", "WED",
                "THU", "FRI", "SAT"};
        String month[]
                = {"1월", "2월", "3월",
                "4월", "5월", "6월",
                "7월", "8월", "9월",
                "10월", "11월", "12월"};

        int ar[] = {31, 29, 31, 30, 31, 30,
                31, 31, 30, 31, 30, 31};

        while (true) {
            if (d == 1 && m == mm && y == yy) {
                break;
            }

            if (y % 4 == 0 && y % 100 != 0
                    || y % 100 == 0) {
                ar[1] = 29;
            } else {
                ar[1] = 28;
            }
            dy++;
            d++;

            if (d > ar[m - 1]) {
                m++;
                d = 1;
            }

            if (m > 12) {
                m = 1;
                y++;
            }

            if (dy == 7) {
                dy = 0;
            }
        }

        int c = dy;

        if (y % 4 == 0 && y % 100 != 0 || y % 400 == 0) {
            ar[1] = 29;
        } else {
            ar[1] = 28;
        }

        System.out.println("현재: " + yy + "년" + " " + month[mm - 1]);

        for (int k = 0; k < 7; k++) {
            System.out.print("   " + "\033[0;1m" + day[k] + "\033[0m" + "   ");
        }

        System.out.println();
        System.out.println();

        for (int j = 1; j <= (ar[mm - 1] + dy); j++) {
            if (j > 6) {
                dy = dy % 7;
            }
        }

        int spaces = dy - 1;
        if (spaces < 0)
            spaces = 6;

        for (int i = 0; i < spaces; i++) {
            System.out.print("         ");
        }

        if (house.getHouseType().equals(HouseType.PERSONAL)) {
            LocalDate localDate = LocalDate.of(yy, mm, 1);
            printPersonal(house, ar, mm, spaces, localDate);
        }
        if (house.getHouseType().equals(HouseType.WHOLE)) {
            printWhole(house, ar, mm, spaces);
        }
    }

    // 숙소 유형이 개인인 숙소의 예약 현황을 달력으로 출력하는 함수
    private void printPersonal(House house, int[] ar, int mm, int spaces, LocalDate localDate) {
        int i = 0;
        int j = 0;
        int k = 1;
        int currentRoom = 0;
        int index = 0;
        boolean isFirstLine = true;
        List<LocalDate> reservationDates = getBookedDates(house);
        Map<LocalDate, Long> dateCountMap = null;
        for (i = 1; i <= ar[mm - 1]; i++) {
            System.out.printf("\033[0;1m" + "%-4d     " + "\033[0m", i);

            if (((i + spaces) % 7 == 0)
                    || (i == ar[mm - 1])) {
                System.out.println();
                System.out.println();
                if (isFirstLine) {
                    for (j = 0; j < spaces; j++) {
                        System.out.print("         ");
                    }
                    isFirstLine = false;
                }
                for (; k <= i && k <= ar[mm - 1]; k++) {
                    localDate = localDate.withDayOfMonth(k);
                    if (reservationDates == null) {
                        currentRoom = house.getAcceptanceInfo().getRoom();
                    } else {
                        dateCountMap = countDates(reservationDates);
                        currentRoom = house.getAcceptanceInfo().getRoom();
                        for (Map.Entry<LocalDate, Long> entry : dateCountMap.entrySet()) {
                            if (localDate.equals(entry.getKey())) {
                                currentRoom = (int) (house.getAcceptanceInfo().getRoom() - entry.getValue());
                                break;
                            }
                        }
                    }
                    System.out.printf("%5d    ", currentRoom);
                }
                k = i + 1;
                System.out.println();
                System.out.println();
            }
        }
    }

    // 숙소 유형이 전체인 숙소의 예약 현황을 달력으로 출력하는 함수
    private void printWhole(House house, int[] ar, int mm, int spaces) {
        int i = 0;
        int j = 0;
        int k = 1;
        String currentRoom = "◯";
        int index = 0;
        boolean isFirstLine = true;
        List<LocalDate> reservationDates = getBookedDates(house);
        for (i = 1; i <= ar[mm - 1]; i++) {
            System.out.printf("\033[0;1m" + "%-4d     " + "\033[0m", i);

            if (((i + spaces) % 7 == 0)
                    || (i == ar[mm - 1])) {
                System.out.println();
                System.out.println();
                if (isFirstLine) {
                    for (j = 0; j < spaces; j++) {
                        System.out.print("         ");
                    }
                    isFirstLine = false;
                }
                for (; k <= i && k <= ar[mm - 1]; k++) {
                    if (reservationDates == null) {
                        currentRoom = "◯";
                    } else if (mm != reservationDates.get(index).getMonthValue()) {
                        currentRoom = "◯";
                    } else if (k == reservationDates.get(index).getDayOfMonth()) {
                        currentRoom = "*";
                        if (index != reservationDates.size() - 1)
                            index++;
                    } else currentRoom = "◯";

                    System.out.printf("%5s    ", currentRoom);
                }
                k = i + 1;
                System.out.println();
                System.out.println();
            }
        }
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

    // 예약 된 모든 날짜를 찾아 리스트를 리턴하는 함수
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
}


