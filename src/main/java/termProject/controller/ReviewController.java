package termProject.controller;

import termProject.entity.Guest;
import termProject.entity.House;
import termProject.entity.Reservation;
import termProject.entity.Review;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ReviewController {
    private final EntityManager em;
    private final EntityTransaction tx;

    public ReviewController(EntityManager em) {
        this.em = em;
        this.tx = em.getTransaction();
    }

    // 리뷰 등록하는 함수
    public void addComment(Long gustId, Long reserveId, int star, String comment) {
        Reservation reservation = em.find(Reservation.class, reserveId);
        Guest guest = em.find(Guest.class, gustId);
        if(reservation == null || guest == null) {
            System.out.println("존재하지 않은 정보입니다.");
            return;
        }
        if(!isCheckedOut(guest, reserveId)) {
            System.out.println("체크아웃이 되지 않았습니다.");
            return;
        }
        if(isExistReview(guest, reserveId)) {
            System.out.println("이미 작성된 리뷰입니다");
            return;
        }
        Review review = Review.create(star, comment);
        try {
            tx.begin();
            review.setReview(guest, reservation.getHouse(), reservation);
            em.persist(review);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        }finally {
            em.clear();
        }
    }

    // 체크아웃 여부를 확인하는 함수
    private boolean isCheckedOut(Guest guest, Long reserveId) {
        List<Reservation> allReservation = guest.getReservationList();
        LocalDate checkout_localDate = null;
        for (Reservation reservation : allReservation) {
            checkout_localDate = LocalDate.ofInstant(reservation.getCheckout().toInstant(), ZoneId.systemDefault());
            if(Objects.equals(reservation.getId(), reserveId)) {
                if (LocalDate.now().isAfter(checkout_localDate) || LocalDate.now().isEqual(checkout_localDate)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 이미 등록된 리뷰인지 확인하는 함수
    private boolean isExistReview(Guest guest, Long reserveId) {
        List<Review> allReview = guest.getReviewList();
        for (Review review : allReview) {
            if(Objects.equals(review.getReservation().getId(), reserveId)) {
                return true;
            }
        }
        return false;
    }

    // 샘플 데이터 생성
    public void createSampleData() {
        try{
            tx.begin();
            Random random = new Random();
            for(int i = 0; i < 20; i++) {
                Integer star =  random.nextInt(3) + 3;
                Long houseId = random.nextLong(3) + 1;
                Long guestId = random.nextLong(4) + 1;
                Review review = Review.create(star, "샘플 리뷰입니다." + i);
                House house = em.find(House.class, houseId);
                Guest guest = em.find(Guest.class, guestId);
                Reservation reservation = em.find(Reservation.class, 5L);
                review.setReview(guest, house, reservation);
                em.persist(review);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println(e);
        }finally {
            em.clear();
        }
    }
}
