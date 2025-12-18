--
-- PostgreSQL database dump
--

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2025-12-18 21:46:32

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', 'public', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 241 (class 1259 OID 26381)
-- Name: booking_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.booking_sessions (
    id uuid NOT NULL,
    booking_time timestamp(6) without time zone NOT NULL,
    client_notes character varying(1000),
    coach_notes character varying(1000),
    status character varying(255) NOT NULL,
    client_id uuid NOT NULL,
    coach_id uuid NOT NULL,
    time_slot_id uuid,
    updated_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    deleted boolean DEFAULT false NOT NULL,
    amount numeric(10,2),
    requires_payment boolean,
    expired_at timestamp without time zone,
    CONSTRAINT booking_sessions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PENDING_PAYMENT'::character varying, 'CONFIRMED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'NO_SHOW'::character varying])::text[])))
);


ALTER TABLE public.booking_sessions OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 26389)
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chat_messages (
    id uuid NOT NULL,
    content text NOT NULL,
    is_read boolean NOT NULL,
    read_at timestamp(6) without time zone,
    sent_at timestamp(6) without time zone NOT NULL,
    type character varying(255) NOT NULL,
    receiver_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    CONSTRAINT chat_messages_type_check CHECK (((type)::text = ANY ((ARRAY['TEXT'::character varying, 'IMAGE'::character varying, 'VIDEO'::character varying, 'FILE'::character varying])::text[])))
);


ALTER TABLE public.chat_messages OWNER TO postgres;

--
-- TOC entry 247 (class 1259 OID 26524)
-- Name: coach_balances; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coach_balances (
    id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    available_balance numeric(15,2) NOT NULL,
    bank_account_info character varying(500),
    last_updated timestamp(6) without time zone,
    pending_balance numeric(15,2) NOT NULL,
    total_earned numeric(15,2) NOT NULL,
    total_withdrawn numeric(15,2) NOT NULL,
    coach_id uuid NOT NULL
);


ALTER TABLE public.coach_balances OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 26362)
-- Name: coach_clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coach_clients (
    coach_client_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    notes text,
    started_date timestamp(6) with time zone,
    status character varying(255) NOT NULL,
    client_id uuid NOT NULL,
    coach_id uuid NOT NULL
);


ALTER TABLE public.coach_clients OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 26397)
-- Name: coach_time_slots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coach_time_slots (
    id uuid NOT NULL,
    end_time timestamp(6) without time zone NOT NULL,
    is_available boolean NOT NULL,
    notes character varying(500),
    start_time timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    coach_id uuid NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    location character varying(255),
    price numeric(10,2),
    capacity integer DEFAULT 1 NOT NULL,
    booked_count integer DEFAULT 0 NOT NULL,
    CONSTRAINT coach_time_slots_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'FULL'::character varying, 'DISABLED'::character varying])::text[])))
);


ALTER TABLE public.coach_time_slots OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 26532)
-- Name: coach_transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coach_transactions (
    id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    amount numeric(15,2) NOT NULL,
    balance_after numeric(15,2) NOT NULL,
    balance_before numeric(15,2) NOT NULL,
    description character varying(1000),
    net_amount numeric(15,2) NOT NULL,
    platform_fee numeric(15,2) NOT NULL,
    processed_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    booking_session_id uuid,
    coach_id uuid NOT NULL,
    payment_id uuid,
    CONSTRAINT coach_transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT coach_transactions_type_check CHECK (((type)::text = ANY ((ARRAY['EARNING'::character varying, 'WITHDRAWAL'::character varying, 'REFUND'::character varying, 'ADJUSTMENT'::character varying])::text[])))
);


ALTER TABLE public.coach_transactions OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16899)
-- Name: community_posts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.community_posts (
    post_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    content character varying(255) NOT NULL,
    status character varying(255),
    title character varying(255),
    user_id uuid,
    report_count integer DEFAULT 0
);


ALTER TABLE public.community_posts OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16906)
-- Name: exercise_media; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exercise_media (
    media_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    caption character varying(255),
    media_type character varying(255) NOT NULL,
    order_no integer,
    url character varying(255) NOT NULL,
    exercise_id uuid NOT NULL
);


ALTER TABLE public.exercise_media OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 17154)
-- Name: exercise_tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exercise_tags (
    exercise_id uuid NOT NULL,
    tag_id uuid NOT NULL
);


ALTER TABLE public.exercise_tags OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16913)
-- Name: exercises; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exercises (
    exercise_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    description text,
    difficulty character varying(255),
    metadata jsonb,
    muscle_group character varying(255),
    name character varying(255) NOT NULL,
    created_by uuid,
    exercise_type character varying(255),
    CONSTRAINT exercises_exercise_type_check CHECK (((exercise_type)::text = ANY ((ARRAY['WEIGHT_AND_REPS'::character varying, 'BODYWEIGHT_REPS'::character varying, 'REPS_ONLY'::character varying, 'TIME_BASED'::character varying, 'DISTANCE_BASED'::character varying, 'WEIGHT_AND_TIME'::character varying, 'ASSISTED_BODYWEIGHT'::character varying])::text[])))
);


ALTER TABLE public.exercises OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16920)
-- Name: health_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.health_logs (
    health_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    body_fat_percent double precision,
    heart_rate integer,
    muscle_mass_kg double precision,
    note character varying(255),
    recorded_at timestamp(6) with time zone NOT NULL,
    waist_cm double precision,
    weight_kg double precision,
    user_id uuid NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone
);


ALTER TABLE public.health_logs OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 26473)
-- Name: notification_preferences; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notification_preferences (
    id uuid NOT NULL,
    daily_reminder boolean NOT NULL,
    email_achievements boolean NOT NULL,
    email_booking boolean NOT NULL,
    email_coach boolean NOT NULL,
    email_enabled boolean NOT NULL,
    email_reminders boolean NOT NULL,
    email_social boolean NOT NULL,
    email_workouts boolean NOT NULL,
    push_achievements boolean NOT NULL,
    push_booking boolean NOT NULL,
    push_coach boolean NOT NULL,
    push_enabled boolean NOT NULL,
    push_reminders boolean NOT NULL,
    push_social boolean NOT NULL,
    push_workouts boolean NOT NULL,
    reminder_time character varying(255),
    user_id uuid NOT NULL
);


ALTER TABLE public.notification_preferences OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 26478)
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email_sent boolean NOT NULL,
    is_read boolean NOT NULL,
    link character varying(255),
    message character varying(500) NOT NULL,
    metadata text,
    push_sent boolean NOT NULL,
    read_at timestamp(6) without time zone,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['ACHIEVEMENT'::character varying, 'WORKOUT'::character varying, 'STREAK'::character varying, 'SYSTEM'::character varying, 'REMINDER'::character varying, 'SOCIAL'::character varying, 'COACH_MESSAGE'::character varying, 'BOOKING_CONFIRMED'::character varying, 'BOOKING_CANCELLED'::character varying, 'PROGRAM_UPDATE'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16939)
-- Name: nutrition_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.nutrition_logs (
    nutrition_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    calories integer,
    carbs_g integer,
    fats_g integer,
    log_date date,
    meal_type character varying(255),
    notes character varying(255),
    protein_g integer,
    user_id uuid NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone
);


ALTER TABLE public.nutrition_logs OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 26293)
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_tokens (
    token_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    expiry_date timestamp(6) with time zone NOT NULL,
    is_used boolean NOT NULL,
    reset_token character varying(255) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.password_reset_tokens OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 26498)
-- Name: payments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payments (
    id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    amount numeric(10,2) NOT NULL,
    description character varying(1000),
    failure_reason character varying(500),
    order_id character varying(255),
    paid_at timestamp(6) without time zone,
    payment_method character varying(255) NOT NULL,
    payment_response character varying(2000),
    status character varying(255) NOT NULL,
    transaction_id character varying(255),
    booking_session_id uuid,
    user_id uuid NOT NULL,
    metadata character varying(2000),
    client_id uuid,
    coach_id uuid,
    CONSTRAINT payments_payment_method_check CHECK (((payment_method)::text = ANY ((ARRAY['MOMO'::character varying, 'VNPAY'::character varying, 'ZALOPAY'::character varying, 'BANK_TRANSFER'::character varying, 'CASH'::character varying])::text[]))),
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'REFUNDED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.payments OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16946)
-- Name: post_comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_comments (
    comment_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    content character varying(255) NOT NULL,
    status character varying(255),
    parent_comment_id uuid,
    post_id uuid NOT NULL,
    user_id uuid
);


ALTER TABLE public.post_comments OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16953)
-- Name: post_likes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_likes (
    like_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    post_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.post_likes OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16958)
-- Name: post_media; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_media (
    post_media_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    media_type character varying(255),
    order_no integer,
    url character varying(255),
    post_id uuid NOT NULL
);


ALTER TABLE public.post_media OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 26344)
-- Name: post_reports; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.post_reports (
    report_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    description text,
    reason character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    post_id uuid NOT NULL,
    reporter_id uuid NOT NULL
);


ALTER TABLE public.post_reports OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16965)
-- Name: program_exercises; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.program_exercises (
    program_exercise_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    day_of_program integer,
    notes character varying(255),
    order_no integer,
    reps character varying(255),
    rest_seconds integer,
    sets integer,
    weight_scheme character varying(255),
    exercise_id uuid NOT NULL,
    program_id uuid NOT NULL
);


ALTER TABLE public.program_exercises OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 18053)
-- Name: program_media; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.program_media (
    id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    caption character varying(255),
    media_type character varying(255),
    order_no integer,
    url character varying(255),
    program_id uuid
);


ALTER TABLE public.program_media OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 26305)
-- Name: program_ratings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.program_ratings (
    rating_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    rating integer NOT NULL,
    review character varying(1000),
    program_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.program_ratings OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16972)
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    token_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    expiry_date timestamp(6) with time zone NOT NULL,
    is_revoked boolean NOT NULL,
    refresh_token character varying(255) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16977)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    role_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    name character varying(255) NOT NULL,
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_COACH'::character varying, 'ROLE_ADMIN'::character varying, 'ROLE_SUPER_ADMIN'::character varying])::text[])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 26312)
-- Name: saved_programs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.saved_programs (
    saved_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    program_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.saved_programs OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16983)
-- Name: session_exercise_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.session_exercise_logs (
    log_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    reps_done integer,
    set_number integer,
    weight_used double precision,
    exercise_id uuid NOT NULL,
    session_id uuid NOT NULL,
    body_weight double precision,
    distance_meters double precision,
    duration_seconds integer,
    set_notes character varying(255),
    updated_at timestamp with time zone,
    deleted boolean DEFAULT false NOT NULL,
    completed boolean DEFAULT false NOT NULL
);


ALTER TABLE public.session_exercise_logs OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 17159)
-- Name: tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tags (
    tag_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone
);


ALTER TABLE public.tags OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16988)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_role_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    assigned_by uuid,
    role_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16993)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    birth_date date,
    body_fat_percent double precision,
    email character varying(255),
    experience_level character varying(255),
    full_name character varying(255),
    gender character varying(255),
    goal character varying(255),
    height_cm integer,
    password character varying(255) NOT NULL,
    weight_kg double precision,
    provider character varying(255),
    avatar_url character varying(500),
    last_login timestamp(6) with time zone,
    status character varying(255) DEFAULT 'ACTIVE'::character varying NOT NULL,
    phone character varying(255),
    daily_calorie_goal integer DEFAULT 2000,
    daily_water_goal integer DEFAULT 8,
    daily_workout_mins integer DEFAULT 60,
    dark_mode boolean DEFAULT false,
    language character varying(255),
    notifications boolean DEFAULT true,
    streak_days integer DEFAULT 0,
    total_workouts integer DEFAULT 0,
    total_volume double precision DEFAULT 0.0,
    last_streak_update date,
    CONSTRAINT users_provider_check CHECK (((provider)::text = ANY ((ARRAY['LOCAL'::character varying, 'GOOGLE'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 17000)
-- Name: workout_programs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workout_programs (
    program_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    duration_weeks integer,
    goal character varying(255),
    metadata jsonb,
    title character varying(255) NOT NULL,
    visibility character varying(255),
    creator_user_id uuid,
    description text
);


ALTER TABLE public.workout_programs OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 17007)
-- Name: workout_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.workout_sessions (
    session_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    deleted boolean DEFAULT false NOT NULL,
    updated_at timestamp with time zone,
    duration_minutes integer,
    notes character varying(255),
    session_date timestamp(6) with time zone NOT NULL,
    program_id uuid,
    user_id uuid NOT NULL
);


ALTER TABLE public.workout_sessions OWNER TO postgres;

--
-- TOC entry 5205 (class 0 OID 26381)
-- Dependencies: 241
-- Data for Name: booking_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5206 (class 0 OID 26389)
-- Dependencies: 242
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: chat_messages
INSERT INTO chat_messages (id, content, is_read, read_at, sent_at, type, receiver_id, sender_id) VALUES
    ('3b3e3d45-49d8-415e-9811-ec9efbb6aacc', 'HI', 't', '2025-12-14 14:14:42.578531', '2025-12-14 14:14:38.997544', 'TEXT', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('154d3b73-c8bc-4764-bae2-93d436b37e8a', 'HI', 't', '2025-12-14 14:14:54.983771', '2025-12-14 14:14:48.088006', 'TEXT', '8a79865b-85ed-4487-8a77-c560bb853ede', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('ac7cc742-0d4c-4d7a-b7be-d1f14673f7a1', 'HI', 't', '2025-12-14 14:15:31.247931', '2025-12-14 14:15:11.281779', 'TEXT', '8a79865b-85ed-4487-8a77-c560bb853ede', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('ec2ffef9-9890-42a1-84f2-4fa4986c9766', 'HI', 't', '2025-12-14 14:15:37.204306', '2025-12-14 14:15:00.407932', 'TEXT', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 5211 (class 0 OID 26524)
-- Dependencies: 247
-- Data for Name: coach_balances; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: coach_balances
INSERT INTO coach_balances (id, created_at, deleted, updated_at, available_balance, bank_account_info, last_updated, pending_balance, total_earned, total_withdrawn, coach_id) VALUES
    ('968a186f-e352-4796-9c2a-8f37a699e5aa', '2025-12-14 13:16:28.105641+07', 'f', '2025-12-18 00:43:10.77793+07', '35000.00', 'ưerwewe', '2025-12-18 00:43:10.777931', '2953800.00', '85000.00', '50000.00', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9');



--
-- TOC entry 5204 (class 0 OID 26362)
-- Dependencies: 240
-- Data for Name: coach_clients; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5207 (class 0 OID 26397)
-- Dependencies: 243
-- Data for Name: coach_time_slots; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: coach_time_slots
INSERT INTO coach_time_slots (id, end_time, is_available, notes, start_time, status, coach_id, deleted, updated_at, created_at, location, price, capacity, booked_count) VALUES
    ('618e02b6-eb78-4315-bf3e-82a4ca55b963', '2025-12-23 13:00:00', 't', NULL, '2025-12-23 11:00:00', 'AVAILABLE', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9', 'f', '2025-12-18 21:42:36.462298+07', '2025-12-18 21:42:36.462298+07', 'Phòng Tập Nam Văn, HVB, Quận 12', '200000.00', '1', '0'),
    ('d63c6198-74c6-4538-aa59-b7df0f28d388', '2025-12-27 13:00:00', 't', NULL, '2025-12-27 11:00:00', 'AVAILABLE', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9', 'f', '2025-12-18 21:43:06.842143+07', '2025-12-18 21:43:06.842143+07', 'Phòng Tập CMC, HVB, Quận 12', '300000.00', '1', '0');



--
-- TOC entry 5212 (class 0 OID 26532)
-- Dependencies: 248
-- Data for Name: coach_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5181 (class 0 OID 16899)
-- Dependencies: 217
-- Data for Name: community_posts; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: community_posts
INSERT INTO community_posts (post_id, created_at, deleted, updated_at, content, status, title, user_id, report_count) VALUES
    ('5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '2025-11-26 00:24:35.560986+07', 'f', '2025-12-04 12:17:20.859842+07', 'Ngày 1 ....', 'active', 'Luyện tập 30 ngày', '299f7701-a20b-49ff-adda-a748525cc7c0', '0'),
    ('608c432c-a99c-4ba2-9cdd-d5c95c12a100', '2025-12-04 20:42:33.397886+07', 'f', '2025-12-16 20:27:17.180698+07', 'Ngày 1: Hãy tập nhưng bài tập đơn giản dưới đây\\n....', 'active', 'Hướng dẫn luyện tập cho người mới bắt đầu', '37281177-8b54-45ad-bf37-568deb8e33cd', '0'),
    ('ea75b418-0307-42b0-a3d4-872015982244', '2025-11-26 00:20:53.844018+07', 'f', '2025-12-18 04:52:29.873451+07', 'Dinh dương cho người mới bắt đầu\\nĐậu nành, trứng, hạt...', 'active', 'Dinh dương cho người mới bắt đầu', '8a79865b-85ed-4487-8a77-c560bb853ede', '0');



--
-- TOC entry 5182 (class 0 OID 16906)
-- Dependencies: 218
-- Data for Name: exercise_media; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: exercise_media
INSERT INTO exercise_media (media_id, created_at, deleted, updated_at, caption, media_type, order_no, url, exercise_id) VALUES
    ('ea4f5a6e-9f1a-4866-b71f-293759f39d8f', '2025-12-18 21:28:30.747884+07', 'f', '2025-12-18 21:28:30.747884+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068109/viegym/ftni5pxt9abpmnm8ruti.webp', 'd851ba0e-eb60-4e75-8ff4-ca29244efca7'),
    ('2987d1fe-ac23-4d85-a9b1-2f701d8d5d6a', '2025-11-26 00:33:28.949971+07', 'f', '2025-11-26 00:33:28.949971+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092009/viegym/mxm76hgnuyvnjcixlnen.webp', '82e565ce-364b-4ae8-8843-aebcd02fd111'),
    ('5a0442e9-c393-4958-856c-6b1bf98b2a4b', '2025-11-26 00:34:59.925921+07', 'f', '2025-11-26 00:34:59.925921+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092100/viegym/rxuqqcensy8qhtsmmdoq.webp', 'cbf9614d-2054-4589-b3a0-d654afdf9a62'),
    ('abc9dcc0-0ec1-4eb2-b455-67a6c713b971', '2025-11-26 00:35:07.072172+07', 'f', '2025-11-26 00:35:07.072172+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092107/viegym/ciiwikl810bvdu5qbekh.webp', '54cf33f3-0b25-447c-be9e-b6a8043856ec'),
    ('87a7a348-f0d0-4447-8dc0-0a90e959de74', '2025-11-26 00:35:16.188943+07', 'f', '2025-11-26 00:35:16.188943+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092117/viegym/lcsj8spncqr1xzgjynvj.webp', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb'),
    ('989cf713-ea74-4f10-8d2f-9d82cb56ddee', '2025-11-26 00:35:32.448675+07', 'f', '2025-11-26 00:35:32.448675+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092132/viegym/hi5y9kcoxrtg0gq3uges.webp', 'deb5eaf1-686b-4f53-a0b2-4cbb2f76a6d2'),
    ('440701c4-b3be-44b7-9763-c7ec8d0f68c6', '2025-11-26 00:35:41.571532+07', 'f', '2025-11-26 00:35:41.571532+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092142/viegym/ulkz8vgziwrbtxrf56sa.webp', '60944745-c6ed-4136-ac50-f27862fac26b'),
    ('935fe62c-6164-481b-aed7-8528bb1c5069', '2025-11-26 00:35:50.299551+07', 'f', '2025-11-26 00:35:50.299551+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092150/viegym/nglkmmum8regsbgwrtaq.webp', '9a53d268-13e1-4ea9-a0f5-e7da1a19a0a2'),
    ('390529cf-7c1b-4341-992e-d5fe06c5c119', '2025-11-26 00:37:16.994254+07', 'f', '2025-11-26 00:37:16.994254+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092237/viegym/l1p0prusiyolbev1t4vo.webp', '07f9990d-ac04-4066-a81e-4eb2b17e8628'),
    ('98531cb8-5452-4d5e-a609-1de767c1a857', '2025-11-26 00:37:41.598012+07', 'f', '2025-11-26 00:37:41.598012+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764092262/viegym/bbleken0if61nnay5tm7.webp', 'ccabd244-1416-416b-9cd1-b09aa817787e'),
    ('f62b3bb6-9479-4ef4-a47c-2a99059f67fd', '2025-12-18 21:10:10.49112+07', 'f', '2025-12-18 21:10:10.49112+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766066950/viegym/ncsrxn3zj2wnbwmczjmv.webp', 'c43c802d-d8bc-4d68-ad08-f05b3047d8bc'),
    ('350cf1dc-70d0-4db8-86fc-550d27cee1fd', '2025-12-18 21:14:49.445605+07', 'f', '2025-12-18 21:14:49.445605+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067261/viegym/aixhvrdjoy7c7g2f2dwj.webp', 'b602c0d3-d757-4fb5-bd44-b484e5da6fa8'),
    ('ef6ef4a6-18de-4cfa-be7d-de7183bacbd7', '2025-12-18 21:15:33.453879+07', 'f', '2025-12-18 21:15:33.453879+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067331/viegym/daxd9qt0tfawkp4imzqa.webp', 'f8f8187b-6f43-4732-af56-edf961ac16da'),
    ('8b636762-fdd0-4e2d-98fc-c125df393114', '2025-12-18 21:16:15.086243+07', 'f', '2025-12-18 21:16:15.086243+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067374/viegym/g51rswpdhflog4v10oit.webp', '854f43bb-7cc9-42eb-8152-5ddac498dc85'),
    ('03b9d4ad-179f-40c4-af27-b242bc1b62c1', '2025-12-18 21:19:14.862978+07', 'f', '2025-12-18 21:19:14.862978+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067553/viegym/hmhiihughzmmvuzt4eem.webp', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629'),
    ('048cb4a6-c7ff-4370-af2b-c0d0617e6632', '2025-12-18 21:22:33.945867+07', 'f', '2025-12-18 21:22:33.945867+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067752/viegym/ukqbq5klrotmjzd9qogf.webp', '291050a3-939c-46a6-b71a-1db42e42da60'),
    ('e76a4159-f7ac-4845-be09-7eb288f222ec', '2025-12-18 21:23:34.520848+07', 'f', '2025-12-18 21:23:34.520848+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067812/viegym/usz6o1l9bdej5y1kqicf.webp', '02729b7d-e27f-43e5-bf5a-e56c44592180'),
    ('15aa58d9-f6b7-47ff-96e2-bf2ff952fea3', '2025-12-18 21:26:05.652808+07', 'f', '2025-12-18 21:26:05.652808+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766067963/viegym/erhd0elmiyzw9pbs8mrp.webp', '889716ea-a96b-4222-88d5-510a6e2655fd'),
    ('6223b798-2bf9-4b4e-b8b6-e65c5ebe83cd', '2025-12-18 21:27:09.518003+07', 'f', '2025-12-18 21:27:09.518003+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068028/viegym/fshd0m6vrxmdn1xb8c0i.webp', '890f051f-1c35-422c-b7f4-693714809505'),
    ('348b12c1-734c-45e9-8890-e46d09f3b35e', '2025-12-18 21:29:36.618912+07', 'f', '2025-12-18 21:29:36.618912+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068175/viegym/aww54xbr8wu1ttusm0av.webp', 'dd7fa767-c813-4134-99e6-1af95257bf91'),
    ('0e2bb112-2fe5-443c-b76f-e67a2f68c525', '2025-12-18 21:30:40.63162+07', 'f', '2025-12-18 21:30:40.63162+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068239/viegym/u21ju9k6arikpaydiomq.webp', '03d47f26-bc2b-4e41-b3b1-0fccb6eb88b4'),
    ('e0780114-7759-4953-81c1-9b70ec5c8cbb', '2025-12-18 21:31:16.011543+07', 'f', '2025-12-18 21:31:16.011543+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068274/viegym/uksiex6mjskuehmoegod.webp', '0506286a-a6e4-4dc3-b2b8-46a6ff2f478f'),
    ('6973ca76-c785-4afc-8df0-20a531bf94cc', '2025-12-18 21:32:00.1105+07', 'f', '2025-12-18 21:32:00.1105+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068318/viegym/b0lo0utgtcdpbq7y3dmu.webp', '69cd468f-0e75-4c0c-ada8-1ebd6bd5abf3'),
    ('4a1582a0-6e40-4ea2-b680-a44d441561fe', '2025-12-18 21:32:36.48268+07', 'f', '2025-12-18 21:32:36.48268+07', NULL, 'IMAGE', '0', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766068355/viegym/wsofefohxmwaybdz8gek.webp', '471bdf12-2806-4d9a-8a32-cc8b0cb85978');



--
-- TOC entry 5197 (class 0 OID 17154)
-- Dependencies: 233
-- Data for Name: exercise_tags; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: exercise_tags
INSERT INTO exercise_tags (exercise_id, tag_id) VALUES
    ('c43c802d-d8bc-4d68-ad08-f05b3047d8bc', '78936936-accc-4bc0-9c53-8c1b946c8815'),
    ('b602c0d3-d757-4fb5-bd44-b484e5da6fa8', '03a670c9-8af4-4abc-9f10-550ded0a57cd');



--
-- TOC entry 5183 (class 0 OID 16913)
-- Dependencies: 219
-- Data for Name: exercises; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: exercises
INSERT INTO exercises (exercise_id, created_at, deleted, updated_at, description, difficulty, metadata, muscle_group, name, created_by, exercise_type) VALUES
    ('b17938e9-2d3f-4d54-a826-0e44f3d074eb', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:09:58.357851+07', 'Bài tập thể trọng cổ điển. Bắt đầu ở tư thế plank, hạ cơ thể xuống đến khi ngực gần chạm sàn, đẩy lên.', 'EASY', '{"equipment": ["Không Dụng Cụ"], "primary_muscles": ["Ngực", "Cơ Tam Đầu"], "secondary_muscles": ["Core", "Vai"]}', 'Ngực', 'Chống Đẩy', NULL, 'BODYWEIGHT_REPS'),
    ('c43c802d-d8bc-4d68-ad08-f05b3047d8bc', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:10:10.494833+07', 'Chống đẩy với tay tạo hình kim cương dưới ngực. Tập trung vào ngực trong và cơ tam đầu.', 'MEDIUM', '{"equipment": ["Không Dụng Cụ"], "primary_muscles": ["Ngực Trong", "Cơ Tam Đầu"], "secondary_muscles": ["Vai"]}', 'Ngực', 'Chống Đẩy Kim Cương', NULL, 'BODYWEIGHT_REPS'),
    ('b602c0d3-d757-4fb5-bd44-b484e5da6fa8', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:14:49.445605+07', 'Biến thể deadlift bắt đầu từ vị trí nâng cao (chốt hoặc khối). Tập trung lưng trên và cơ thang.', 'HARD', '{"equipment": ["Tạ Đòn", "Giá Đỡ Điện"], "primary_muscles": ["Lưng Trên", "Cơ Thang"], "secondary_muscles": ["Lưng Dưới", "Cẳng Tay"]}', 'Lưng', 'Kéo Giá Đỡ', NULL, 'BODYWEIGHT_REPS'),
    ('f8f8187b-6f43-4732-af56-edf961ac16da', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:15:33.455386+07', 'Xen kẽ khuỷu tay vào đầu gối đối diện theo chuyển động xe đạp. Hiệu quả cho cơ chéo và bụng.', 'EASY', '{"equipment": ["Không Dụng Cụ"], "primary_muscles": ["Cơ Thẳng Bụng", "Cơ Chéo"], "secondary_muscles": ["Cơ Gập Hông"]}', 'Core', 'Gập Bụng Xe Đạp', NULL, 'DISTANCE_BASED'),
    ('854f43bb-7cc9-42eb-8152-5ddac498dc85', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:16:15.086243+07', 'Ngồi với chân nâng cao, xoay thân qua lại hai bên. Tập cơ chéo và xoay core.', 'EASY', '{"equipment": ["Tạ Đĩa hoặc Bóng Dược (Tùy Chọn)"], "primary_muscles": ["Cơ Chéo"], "secondary_muscles": ["Cơ Thẳng Bụng"]}', 'Core', 'Xoay Người Nga', NULL, 'BODYWEIGHT_REPS'),
    ('b8db462d-a035-4ee1-bdb9-e87e3feb3629', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:19:14.867584+07', 'Quỳ gối tại máy cáp và gập xuống. Cho phép tăng tải tiến bộ trên bụng.', 'MEDIUM', '{"equipment": ["Máy Cáp"], "primary_muscles": ["Cơ Thẳng Bụng"], "secondary_muscles": ["Cơ Chéo"]}', 'Core', 'Gập Bụng Cáp', NULL, 'REPS_ONLY'),
    ('291050a3-939c-46a6-b71a-1db42e42da60', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:22:33.95004+07', 'Nằm ngửa, cuộn vai về phía hông. Bài tập bụng cổ điển.', 'EASY', '{"equipment": ["Không Dụng Cụ"], "primary_muscles": ["Cơ Thẳng Bụng"], "secondary_muscles": []}', 'Core', 'Gập Bụng', NULL, 'REPS_ONLY'),
    ('02729b7d-e27f-43e5-bf5a-e56c44592180', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:23:34.522848+07', 'Giữ tư thế cơ thể cứng nhắc trên cẳng tay và ngón chân. Bài tập ổn định core cơ bản.', 'EASY', '{"equipment": ["Không Dụng Cụ"], "primary_muscles": ["Cơ Thẳng Bụng", "Cơ Ngang Bụng"], "secondary_muscles": ["Cơ Chéo", "Lưng Dưới"]}', 'Core', 'Plank', NULL, 'TIME_BASED'),
    ('889716ea-a96b-4222-88d5-510a6e2655fd', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:26:05.655334+07', 'Vua của tất cả các bài tập. Nâng tạ đòn từ sàn bằng cách duỗi hông và đầu gối. Giữ lưng thẳng trong suốt động tác.', 'HARD', '{"equipment": ["Tạ Đòn"], "primary_muscles": ["Cơ Dựng Sống", "Cơ Lưng Xô", "Cơ Thang"], "secondary_muscles": ["Mông", "Gân Kheo", "Cẳng Tay"]}', 'Lưng', 'Nâng Tạ Đòn', NULL, 'WEIGHT_AND_REPS'),
    ('890f051f-1c35-422c-b7f4-693714809505', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:27:09.520143+07', 'Treo trên xà với lòng bàn tay úp. Kéo cơ thể lên cho đến khi cằm vượt qua xà. Bài tập lưng cổ điển.', 'MEDIUM', '{"equipment": ["Xà Đơn"], "primary_muscles": ["Cơ Lưng Xô", "Lưng Trên"], "secondary_muscles": ["Cơ Nhị Đầu", "Cẳng Tay"]}', 'Lưng', 'Kéo Xà Đơn', NULL, 'WEIGHT_AND_REPS'),
    ('d851ba0e-eb60-4e75-8ff4-ca29244efca7', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:28:30.748906+07', 'Kéo xà đơn với lòng bàn tay ngửa. Cơ nhị đầu tham gia nhiều hơn kéo xà đơn tiêu chuẩn.', 'MEDIUM', '{"equipment": ["Xà Đơn"], "primary_muscles": ["Cơ Lưng Xô", "Cơ Nhị Đầu"], "secondary_muscles": ["Lưng Trên"]}', 'Lưng', 'Squat', NULL, 'WEIGHT_AND_REPS'),
    ('dd7fa767-c813-4134-99e6-1af95257bf91', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:29:36.618912+07', 'Trên ghế siêu duỗi, cúi ở thắt lưng và duỗi lưng về song song. Tăng cường lưng dưới và mông.', 'EASY', '{"equipment": ["Ghế Siêu Duỗi"], "primary_muscles": ["Lưng Dưới", "Mông"], "secondary_muscles": ["Gân Kheo"]}', 'Lưng', 'Gập Lưng Siêu Duỗi', NULL, 'WEIGHT_AND_REPS'),
    ('ccabd244-1416-416b-9cd1-b09aa817787e', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:37:41.598012+07', 'Nằm trên ghế phẳng với tạ đơn duỗi thẳng. Hạ tạ theo cung tròn với khuỷu tay hơi gập cho đến khi cảm thấy căng ngực.', 'EASY', '{"equipment": ["Tạ Đơn", "Ghế Tập"], "primary_muscles": ["Cơ Ngực Lớn"], "secondary_muscles": ["Cơ Delta Trước"]}', 'Ngực', 'Bay Ngực Tạ Đơn', NULL, 'WEIGHT_AND_REPS'),
    ('cbf9614d-2054-4589-b3a0-d654afdf9a62', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:34:59.926549+07', 'Thực hiện đẩy ngực trên ghế dốc xuống để tập trung vào ngực dưới.', 'MEDIUM', '{"equipment": ["Tạ Đòn", "Ghế Dốc Xuống"], "primary_muscles": ["Ngực Dưới"], "secondary_muscles": ["Cơ Tam Đầu"]}', 'Ngực', 'Đẩy Tạ Đòn Ghế Dốc Xuống', NULL, 'WEIGHT_AND_REPS'),
    ('54cf33f3-0b25-447c-be9e-b6a8043856ec', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:35:07.072172+07', 'Đứng giữa máy cáp với tay cầm ở độ cao vai. Kéo tay cầm về phía trước ngực như động tác ôm.', 'MEDIUM', '{"equipment": ["Máy Cáp"], "primary_muscles": ["Ngực"], "secondary_muscles": ["Vai"]}', 'Ngực', 'Kéo Cáp Chéo Ngực', NULL, 'WEIGHT_AND_REPS'),
    ('deb5eaf1-686b-4f53-a0b2-4cbb2f76a6d2', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:35:32.448675+07', 'Đặt cáp ở vị trí thấp và thực hiện bay ngực trên ghế nghiêng để tập trung ngực trên với lực căng liên tục.', 'MEDIUM', '{"equipment": ["Máy Cáp", "Ghế Nghiêng"], "primary_muscles": ["Ngực Trên"], "secondary_muscles": ["Vai"]}', 'Ngực', 'Bay Ngực Cáp Ghế Nghiêng', NULL, 'WEIGHT_AND_REPS'),
    ('9a53d268-13e1-4ea9-a0f5-e7da1a19a0a2', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:35:50.299551+07', 'Ngồi thẳng trong máy với cánh tay trên miếng đệm. Khép miếng đệm lại phía trước ngực, tập trung co cơ.', 'EASY', '{"equipment": ["Máy Pec Deck"], "primary_muscles": ["Ngực"], "secondary_muscles": ["Cơ Delta Trước"]}', 'Ngực', 'Bay Ngực Máy Pec Deck', NULL, 'WEIGHT_AND_REPS'),
    ('60944745-c6ed-4136-ac50-f27862fac26b', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:35:41.572531+07', 'Máy đẩy ngực ngồi cung cấp chuyển động ổn định, lý tưởng cho người mới hoặc cô lập cơ.', 'EASY', '{"equipment": ["Máy Đẩy Ngực"], "primary_muscles": ["Ngực"], "secondary_muscles": ["Cơ Tam Đầu", "Vai"]}', 'Ngực', 'Đẩy Ngực Máy', NULL, 'WEIGHT_AND_REPS'),
    ('07f9990d-ac04-4066-a81e-4eb2b17e8628', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:37:16.994254+07', 'Đẩy tạ đơn trên ghế nghiêng (30-45 độ) để tập trung vào ngực trên. Hạ từ từ và đẩy lên mạnh mẽ.', 'MEDIUM', '{"equipment": ["Tạ Đơn", "Ghế Nghiêng"], "primary_muscles": ["Ngực Trên"], "secondary_muscles": ["Cơ Tam Đầu", "Vai"]}', 'Ngực', 'Đẩy Tạ Đơn Ghế Nghiêng', NULL, 'WEIGHT_AND_REPS'),
    ('03d47f26-bc2b-4e41-b3b1-0fccb6eb88b4', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:30:40.632582+07', 'Giữ tay thẳng và kéo xà cáp xuống đùi. Cô lập cơ lưng xô với ít sự tham gia của cơ nhị đầu.', 'MEDIUM', '{"equipment": ["Máy Cáp"], "primary_muscles": ["Cơ Lưng Xô"], "secondary_muscles": ["Cơ Tròn Lớn", "Vai Sau"]}', 'Lưng', 'Kéo Xô Tay Thẳng', NULL, 'WEIGHT_AND_REPS'),
    ('0506286a-a6e4-4dc3-b2b8-46a6ff2f478f', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:31:16.012603+07', 'Đẩy tạ đơn ở tư thế ngồi hoặc đứng. Cho phép biên độ chuyển động lớn hơn phiên bản tạ đòn.', 'EASY', '{"equipment": ["Tạ Đơn"], "primary_muscles": ["Cơ Delta"], "secondary_muscles": ["Cơ Tam Đầu", "Ngực Trên"]}', 'Vai', 'Đẩy Vai Tạ Đơn', NULL, 'WEIGHT_AND_REPS'),
    ('69cd468f-0e75-4c0c-ada8-1ebd6bd5abf3', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:32:00.113807+07', 'Đẩy tạ đòn từ phía trước sang phía sau đầu liên tục. Bài tập vai nâng cao.', 'HARD', '{"equipment": ["Tạ Đòn"], "primary_muscles": ["Tất Cả Đầu Cơ Delta"], "secondary_muscles": ["Cơ Tam Đầu", "Cơ Thang"]}', 'Vai', 'Đẩy Bradford', NULL, 'WEIGHT_AND_REPS'),
    ('471bdf12-2806-4d9a-8a32-cc8b0cb85978', '2025-11-25 23:34:09.349028+07', 'f', '2025-12-18 21:32:36.48419+07', 'Vua của các bài tập chân. Đặt tạ lên lưng trên, squat xuống giữ đầu gối theo hướng ngón chân.', 'MEDIUM', '{"equipment": ["Tạ Đòn", "Giá Đỡ Squat"], "primary_muscles": ["Cơ Tứ Đầu Đùi", "Mông"], "secondary_muscles": ["Gân Kheo", "Core", "Lưng Dưới"]}', 'Chân', 'Squat Tạ Đòn', NULL, 'WEIGHT_AND_REPS'),
    ('82e565ce-364b-4ae8-8843-aebcd02fd111', '2025-11-25 23:34:09.349028+07', 'f', '2025-11-26 00:33:28.956105+07', 'Vua của các bài tập ngực. Nằm trên ghế phẳng và đẩy tạ đòn từ mức ngực lên đến khi cánh tay duỗi thẳng hoàn toàn.', 'MEDIUM', '{"equipment": ["Tạ Đòn", "Ghế Tập"], "primary_muscles": ["Cơ Ngực Lớn"], "secondary_muscles": ["Cơ Tam Đầu", "Cơ Delta Trước"]}', 'Ngực', 'Đẩy Tạ Đòn Nằm Ngang', NULL, 'WEIGHT_AND_REPS');



--
-- TOC entry 5184 (class 0 OID 16920)
-- Dependencies: 220
-- Data for Name: health_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5208 (class 0 OID 26473)
-- Dependencies: 244
-- Data for Name: notification_preferences; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: notification_preferences
INSERT INTO notification_preferences (id, daily_reminder, email_achievements, email_booking, email_coach, email_enabled, email_reminders, email_social, email_workouts, push_achievements, push_booking, push_coach, push_enabled, push_reminders, push_social, push_workouts, reminder_time, user_id) VALUES
    ('0d87bd22-a017-4179-b45c-5f4a47f74eeb', 'f', 't', 't', 't', 't', 't', 't', 'f', 't', 't', 't', 't', 't', 't', 't', NULL, '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('116e3634-dadc-4abc-af85-71d3568f5bb7', 'f', 't', 't', 't', 't', 't', 't', 'f', 't', 't', 't', 't', 't', 't', 't', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('3278708f-21cf-46e9-82aa-2a818a96490b', 'f', 't', 't', 't', 't', 't', 't', 'f', 't', 't', 't', 't', 't', 't', 't', NULL, '7c2ab5e3-4655-437c-ab37-7be2d3097842'),
    ('50e9b4ec-e41f-456b-98b7-3349c34f6a0d', 'f', 't', 't', 't', 't', 't', 't', 'f', 't', 't', 't', 't', 't', 't', 't', NULL, 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('1d4f3a80-4759-481f-94f5-e828568ced4a', 'f', 't', 't', 't', 't', 't', 't', 'f', 't', 't', 't', 't', 't', 't', 't', NULL, 'e50f699c-fad5-4b88-80cf-613145fa27e6');



--
-- TOC entry 5209 (class 0 OID 26478)
-- Dependencies: 245
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: notifications
INSERT INTO notifications (id, created_at, email_sent, is_read, link, message, metadata, push_sent, read_at, title, type, user_id) VALUES
    ('60f404d3-1004-4d9a-a095-3f807afe4a4d', '2025-12-05 22:01:01.459767', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T07:00 - 2025-12-13T09:00', NULL, 't', '2025-12-06 11:49:29.983357', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('1ea9f3b3-cbe3-4c04-ab12-9528a296ab69', '2025-12-05 21:53:12.955112', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T11:00 - 2025-12-06T13:00', NULL, 't', '2025-12-05 21:53:45.136255', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('0f3a0de9-415e-425b-a091-6ef116f31f22', '2025-12-05 21:29:43.911162', 't', 't', '/coach/bookings', '✅ Đã xác nhận booking với Trần Thị Ngọc lúc 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:31:44.826316', 'Booking đã xác nhận', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('ad1880d6-5a64-4b50-9622-1f0c92b2dcd9', '2025-12-05 21:34:26.991639', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T07:00 - 2025-12-06T09:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('377686c4-8b48-4b10-ae70-d42ee146d996', '2025-12-05 21:54:18.711703', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T11:00 - 2025-12-06T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('135d81c3-a52b-4b82-a1da-2f35f0080a00', '2025-12-05 21:37:01.203187', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T07:00 - 2025-12-06T09:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('b3fd5b11-79c2-4548-850c-0eb714016a9e', '2025-12-05 21:37:18.888738', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T07:00 - 2025-12-06T09:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('1438ec87-906f-4304-a6d4-4f5e30c2225c', '2025-12-06 11:48:14.653659', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-07T00:00 - 2025-12-07T02:00', NULL, 't', '2025-12-06 11:49:30.292989', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d51ecb53-5163-41bb-8c06-5fde5b960fbb', '2025-12-05 21:37:38.215548', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T07:00 - 2025-12-06T09:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('5da63969-044f-43ae-b391-136e4c692993', '2025-12-05 21:54:24.339536', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T11:00 - 2025-12-06T13:00', NULL, 't', '2025-12-05 21:57:13.615362', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('9d5e993d-e188-4b80-b228-1b8e6ebfb1d0', '2025-12-05 21:57:42.098918', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T11:00 - 2025-12-06T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('3e4e782b-34cc-48e6-81cd-31d5f4b8387e', '2025-12-05 21:37:55.475959', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T07:00 - 2025-12-06T09:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('896afc25-54b2-4fd2-acc8-e8ac587efa99', '2025-12-05 21:37:51.914453', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:38:45.810111', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a29f584b-e3d0-4d76-ba60-d3e4f115961e', '2025-12-05 21:37:12.834018', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:38:48.675212', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('c98af0fa-8ba3-4f1a-9a7d-07a7d788cc0e', '2025-12-05 21:34:41.237649', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:38:50.450951', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('6fbc191f-e2a8-48c4-b2f5-8c64b89b4bc5', '2025-12-05 21:37:31.196552', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:38:53.412495', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('89f8c7ee-e607-47cb-9523-3e19373e1d40', '2025-12-05 21:41:51.589421', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T07:00 - 2025-12-06T09:00', NULL, 't', '2025-12-05 21:45:06.404505', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('6b189115-6100-4298-a0ff-48590fdde7bb', '2025-12-05 22:00:00.361895', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-06T11:00 - 2025-12-06T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('71eed5f6-0b76-4080-9ae3-643b053a5f7f', '2025-12-06 11:53:59.891489', 't', 'f', '/chat', 'Trần Thị Ngọc: hi...', NULL, 't', NULL, 'Tin nhắn mới từ Coach', 'COACH_MESSAGE', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5af28e52-e994-4592-ba1c-b2beb05828be', '2025-12-10 00:04:23.838892', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-11T11:00 - 2025-12-11T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('811315fd-1f04-4ae0-80e5-dffe7c68329a', '2025-12-05 21:58:06.292134', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T11:00 - 2025-12-06T13:00', NULL, 't', '2025-12-06 11:49:29.197858', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('4f8c2edd-110a-4181-982b-aead343294ba', '2025-12-05 22:00:07.501263', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-06T11:00 - 2025-12-06T13:00', NULL, 't', '2025-12-06 11:49:29.655576', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d0a72695-6ad3-41a5-ab79-c32ee6225030', '2025-12-09 23:50:36.642117', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-10T11:00 - 2025-12-10T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('abfd6729-53dc-43f0-b480-cdc3a1bee91d', '2025-12-09 23:56:50.036675', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-10T11:00 - 2025-12-10T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('837000b3-92ea-4a78-90f3-46d9722469a2', '2025-12-10 00:03:55.522458', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-11T11:00 - 2025-12-11T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('bd83d75e-68dd-43f8-b9e0-facdd5e0781f', '2025-12-10 00:04:20.238727', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-11T11:00 - 2025-12-11T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a190855c-a7ee-4759-b280-7f3e8987f438', '2025-12-10 00:04:34.814357', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-11T11:00 - 2025-12-11T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('1de8bfc1-5809-4f68-9bf1-b4cd321bedf2', '2025-12-10 00:11:13.693286', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('35a88841-fe1f-4c59-ba4f-b801e631f154', '2025-12-10 00:14:58.704273', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('f8231073-dc5d-4c09-a823-1227552cd2a2', '2025-12-10 00:15:08.376488', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a247c79a-4192-4a7c-adeb-675c7d53ff2e', '2025-12-10 00:27:52.473838', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('cfaa0f1d-1ffc-472c-8a7f-7bb9d1c82152', '2025-12-10 00:28:05.051759', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('afdc29b9-e3de-4659-8244-d1f96d7d5303', '2025-12-10 00:29:40.083378', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('1d01bfc7-39db-4d40-a085-7f4dd511d0eb', '2025-12-10 00:30:03.029583', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5890cac1-1522-4cae-9a90-f76b4c85acec', '2025-12-10 00:30:40.248156', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('fbd5f4dc-a41b-4cbb-837d-4b4050c1e619', '2025-12-10 00:30:50.170948', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('4d8bdead-50c6-453f-8cc0-9ca17cdb1499', '2025-12-10 00:31:39.174091', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('fe4d9ae2-c540-4405-9ba3-fea1dc0f3883', '2025-12-10 00:31:48.547065', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('3ca3b58b-f344-4da1-9bdb-4d64f2e67b22', '2025-12-10 00:32:53.598463', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('43f11be4-5cf7-41a9-a83f-6bcd8f2b1f16', '2025-12-10 00:33:00.135571', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b62c9228-93e7-40da-804e-b2d0e0331554', '2025-12-10 00:34:48.612649', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('d8a7499d-6a6d-47e1-9cfd-c333a68c80f6', '2025-12-10 00:35:03.138408', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '7c2ab5e3-4655-437c-ab37-7be2d3097842'),
    ('ae29a315-f0da-4ecb-aa76-019a10860808', '2025-12-10 00:37:47.969155', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('ac3e4d5e-582b-48d9-9d7c-c5e149a5e5e6', '2025-12-10 00:37:54.000683', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '7c2ab5e3-4655-437c-ab37-7be2d3097842'),
    ('b704b560-ab5b-49bb-b631-c93ad26d897a', '2025-12-10 00:40:44.588673', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('f2689141-9979-4c4c-91b7-d50d74ad2dcb', '2025-12-10 00:40:53.975086', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '7c2ab5e3-4655-437c-ab37-7be2d3097842'),
    ('7081aeec-3fd9-4e36-b510-f731f5eba8d3', '2025-12-10 06:52:29.537243', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('14000a43-2099-40f1-9104-5e7cef9a01fd', '2025-12-10 06:53:06.29396', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('44d1cb28-8f30-45f0-968f-0e5ef1127cde', '2025-12-10 06:54:20.827135', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('a94cc61c-2410-4473-b85e-3ebbd5b384a1', '2025-12-10 06:55:19.313223', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('1d31cafd-b812-4a1c-a114-bced44a19021', '2025-12-10 07:02:19.904392', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('57e2897d-4eac-4d13-8aa3-b52732de633d', '2025-12-10 07:18:58.279867', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T11:00 - 2025-12-15T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('2a4d8489-b56a-4fe3-8e94-c8d9ef9406e6', '2025-12-10 07:19:21.526751', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('23b4037d-7e68-4675-9285-b0e23e8c95de', '2025-12-10 07:19:45.160081', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a7d733e0-51b4-4f52-bc9b-a681150c1966', '2025-12-10 07:29:25.509295', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d5ac81ca-b694-4b03-a972-b7f0eeba11e0', '2025-12-10 07:39:51.26173', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T11:00 - 2025-12-15T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('167c817f-3da2-4879-84ef-e15de30b9d69', '2025-12-10 07:40:14.602036', 't', 'f', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-12T11:00 - 2025-12-12T13:00)', NULL, 't', NULL, 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('e2a9c852-7191-497b-a07c-1a1b000f6197', '2025-12-10 07:40:17.877681', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('c1b6d380-d125-40b3-ae3c-cec7a3c43f3b', '2025-12-10 07:41:36.305548', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('ad035380-5e6b-4ede-b467-db8daf6a34fc', '2025-12-10 07:43:33.053055', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-12T11:00 - 2025-12-12T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5a48aa48-288d-4ebf-ac84-03c50727ca2f', '2025-12-10 07:46:13.090244', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('7ea0c572-32d8-437f-99b9-bf347397e08f', '2025-12-12 23:19:16.288319', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:26.923906', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b7a35f09-86f2-4bbc-9ede-5a4cfaa5d95d', '2025-12-12 23:01:20.314425', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 'f', '2025-12-14 14:38:26.385572', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('f07b0c98-b725-40c5-91f7-12e64967862c', '2025-12-12 23:28:54.585115', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-14 14:38:27.615019', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b0ea9976-a191-4510-870b-6a108f70f845', '2025-12-13 23:01:58.730277', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-14 14:38:30.33542', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d6ef5f3f-6e4f-4c47-a9ad-634633a08be4', '2025-12-12 23:25:26.688361', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:29.297115', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a76572f1-e7f3-4004-ae8d-a7e4671a1ae1', '2025-12-12 23:41:43.471976', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:19.570783', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('978debd3-89cc-4da7-8501-0726e0f63ab2', '2025-12-13 00:14:36.049344', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:20.3753', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('0f9f38c1-7d97-49ce-b977-4d3dfd5fa949', '2025-12-12 23:47:20.660007', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('4edad392-0a5b-4f88-945a-ef5a82cb4c1a', '2025-12-12 23:50:47.026913', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('f03829c6-cbb4-4c27-8718-b89306e43513', '2025-12-12 23:54:27.318178', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('37baeb4e-d26f-4a62-9f0a-a93acdb0ba0b', '2025-12-13 00:17:00.32053', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:20.831661', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('e868f63d-f2a0-4e5d-882b-177058324e47', '2025-12-12 23:15:54.007632', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-13T11:00 - 2025-12-13T13:00', NULL, 't', '2025-12-14 14:38:26.659353', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('abf56673-4c98-45ad-a48e-0503dd75d2ae', '2025-12-12 23:21:08.382128', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 14:38:27.15858', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('54f880dd-068c-4c1b-893a-5aa9cf9e9f78', '2025-12-13 23:02:01.942115', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-14 14:38:30.681493', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a517e973-c0c8-442f-b747-be3009ace96a', '2025-12-13 23:02:32.454666', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-14 14:38:30.919051', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('91a004e1-187d-4d4e-ab50-3da2051c372a', '2025-12-13 23:11:25.502087', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-14T11:00 - 2025-12-14T13:00', NULL, 't', '2025-12-14 14:38:31.329953', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('946eec59-84d3-4bb4-bbd4-503114487425', '2025-12-13 23:16:54.567705', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-14 14:38:32.445469', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('6d56a01f-8f37-4365-b2e3-e763744da51c', '2025-12-14 14:07:38.262209', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T07:00 - 2025-12-15T09:00', NULL, 't', '2025-12-14 14:38:32.918092', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b58e433f-a6c4-439f-a793-47ac0c88bba9', '2025-12-14 14:12:52.930601', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T07:00 - 2025-12-15T09:00', NULL, 't', '2025-12-14 14:38:33.171581', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a7fb8a00-069f-49b9-a900-334bdae893be', '2025-12-14 14:36:29.415436', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 14:38:33.561007', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('53743444-b823-4504-9ca9-29613165fbee', '2025-12-14 14:07:03.540207', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T07:00 - 2025-12-15T09:00', NULL, 't', '2025-12-14 14:39:01.573659', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d3d93de2-26a1-462d-b50d-9f7c4a1353ce', '2025-12-12 23:34:31.595367', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 14:38:19.123824', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('f504d73b-59c3-4674-bc68-f68d8200a26d', '2025-12-13 00:07:35.823033', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 14:38:19.868966', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a775e7d0-97a3-42f7-a33f-56d718ca9d42', '2025-12-14 15:09:46.218289', 't', 'f', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-16T07:00 - 2025-12-16T09:00', NULL, 't', NULL, 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('3f015bf0-27b7-45d6-8b56-15522f275c29', '2025-12-14 16:05:39.660264', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T11:00 - 2025-12-15T13:00)', NULL, 't', '2025-12-14 16:48:50.280977', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('1a891aa4-6a9c-479f-b737-3d2d9aa90ccd', '2025-12-14 16:11:36.592712', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T00:00 - 2025-12-15T02:00)', NULL, 't', '2025-12-14 16:48:50.467842', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('0a25a2bc-0a79-4306-aef7-c8659b5b4c5f', '2025-12-14 16:13:03.833409', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T00:00 - 2025-12-15T02:00', NULL, 't', '2025-12-14 16:48:50.684197', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('fce2aedc-49a9-440f-92c6-69638a6a1b18', '2025-12-14 16:13:42.397594', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 16:48:50.895944', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('6e28ce46-5681-4e75-b5bc-f473ef069548', '2025-12-14 16:14:42.944357', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T11:00 - 2025-12-15T13:00)', NULL, 't', '2025-12-14 16:48:51.077661', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('aec42e28-ca9f-4616-b0af-18e014150af2', '2025-12-14 16:24:46.593028', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T00:00 - 2025-12-15T02:00)', NULL, 't', '2025-12-14 16:48:51.247924', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d7e000ba-1115-422f-b05f-129ffcd9046e', '2025-12-14 16:26:00.282634', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T00:00 - 2025-12-15T02:00', NULL, 't', '2025-12-14 16:48:51.405131', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('ffde0218-3685-4bfd-a412-5ae75974bb8f', '2025-12-14 15:43:46.579622', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T00:00 - 2025-12-15T02:00)', NULL, 't', '2025-12-14 16:48:51.5871', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b767d265-83ac-416f-bb30-706f7ffd95f2', '2025-12-18 03:36:04.528908', 'f', 'f', '/training', 'Bạn đã lưu chương trình: PHÒNG TẬP GIAI ĐOẠN 2', NULL, 't', NULL, 'Chương trình mới', 'PROGRAM_UPDATE', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('0560c7a0-72c1-4bcd-a4b1-756c8d95d398', '2025-12-18 03:38:45.581671', 'f', 'f', '/training', 'Bạn đã lưu chương trình: Bài tập toàn thân', NULL, 't', NULL, 'Chương trình mới', 'PROGRAM_UPDATE', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('30494cfb-5c2d-47a1-903a-f7e17672ba57', '2025-12-18 03:33:36.237807', 'f', 'f', '/training', 'Bạn đã lưu chương trình: Bài tập đẩy kéo chân Ultimate Push Pull Legs Split của Jeff Nippard (PPL)', NULL, 't', NULL, 'Chương trình mới', 'PROGRAM_UPDATE', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('47829dee-ef5f-4ca3-83e9-c6b2f14edfe6', '2025-12-14 15:13:52.014322', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-16T07:00 - 2025-12-16T09:00)', NULL, 't', '2025-12-14 16:48:46.868518', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('b318b9c5-4dc4-4434-a81e-39643506f9c7', '2025-12-14 15:34:54.503302', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-18T07:00 - 2025-12-18T09:00', NULL, 't', '2025-12-14 16:48:47.167538', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('0ed3dfe4-768e-4248-80ca-3d7da2c98f27', '2025-12-14 15:40:54.771386', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T00:00 - 2025-12-15T02:00', NULL, 't', '2025-12-14 16:48:47.4373', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('849e7cf1-6fad-4828-a347-d4a9b5cf76b0', '2025-12-14 15:50:57.849969', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T00:00 - 2025-12-15T02:00', NULL, 't', '2025-12-14 16:48:47.65511', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede');

INSERT INTO notifications (id, created_at, email_sent, is_read, link, message, metadata, push_sent, read_at, title, type, user_id) VALUES
    ('83a8b4e5-f68e-40b4-9df0-2988f20c04a3', '2025-12-14 15:27:10.04966', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-16T07:00 - 2025-12-16T09:00', NULL, 't', '2025-12-14 16:48:47.953947', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('fd9a61c1-6bf1-4bbf-95b4-85213898d3ec', '2025-12-14 15:51:52.260362', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T00:00 - 2025-12-15T02:00)', NULL, 't', '2025-12-14 16:48:48.641508', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('c4f4c586-0c50-4897-9e00-c67e12c25b3c', '2025-12-14 15:53:08.492026', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 16:48:49.234206', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a5059b4f-7bc3-49ef-b54a-0b683266cbe2', '2025-12-14 16:03:35.985985', 't', 't', '/booking', 'Đã hủy lịch với Trần Thị Ngọc (2025-12-15T11:00 - 2025-12-15T13:00)', NULL, 't', '2025-12-14 16:48:49.643194', 'Lịch đã bị hủy', 'BOOKING_CANCELLED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('c2ab201e-d5b0-4108-846e-04a251b9ef5c', '2025-12-14 16:03:57.424842', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T00:00 - 2025-12-15T02:00', NULL, 't', '2025-12-14 16:48:49.869473', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5b68a8f3-3bc6-4b72-a262-b86ac4daee93', '2025-12-14 16:04:30.704739', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-15T11:00 - 2025-12-15T13:00', NULL, 't', '2025-12-14 16:48:50.074843', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('4d6952a7-7107-4344-8d54-03d6491eb602', '2025-12-18 03:04:22.465379', 'f', 't', '/training', 'Bạn đã lưu chương trình: Bài tập đẩy kéo chân Ultimate Push Pull Legs Split của Jeff Nippard (PPL)', NULL, 't', '2025-12-18 04:07:00.473047', 'Chương trình mới', 'PROGRAM_UPDATE', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5996b7ab-008d-40d3-afe7-659afab7f2e5', '2025-12-18 00:42:01.639707', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-20T07:00 - 2025-12-20T09:00', NULL, 't', '2025-12-18 04:07:00.838558', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('2e903b44-694e-4c3f-b2f7-2d1ffe46b268', '2025-12-18 00:34:47.795425', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T07:00 - 2025-12-19T09:00', NULL, 't', '2025-12-18 04:07:01.174848', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('e05c7bf4-cfe3-4f51-ab57-d42226254f95', '2025-12-17 23:51:34.165816', 't', 't', '/booking', 'Đã đặt lịch với Trần Thị Ngọc vào 2025-12-19T11:00 - 2025-12-19T13:00', NULL, 't', '2025-12-18 04:07:02.883468', 'Đặt lịch thành công', 'BOOKING_CONFIRMED', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('bebffd06-f01e-436a-9f31-8aa577a76835', '2025-12-18 04:30:38.35212', 'f', 'f', '/training', 'Bạn đã lưu chương trình: Bài tập toàn thân', NULL, 't', NULL, 'Chương trình mới', 'PROGRAM_UPDATE', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 5185 (class 0 OID 16939)
-- Dependencies: 221
-- Data for Name: nutrition_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5200 (class 0 OID 26293)
-- Dependencies: 236
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: password_reset_tokens
INSERT INTO password_reset_tokens (token_id, created_at, deleted, updated_at, expiry_date, is_used, reset_token, user_id) VALUES
    ('25e1fcf9-86bd-4438-b02d-9cdccc936969', '2025-12-01 14:59:38.128396+07', 'f', '2025-12-01 14:59:38.128396+07', '2025-12-01 15:59:38.126875+07', 'f', '1900fe78-7818-44da-ad15-5cfca1d60e15', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('bf283685-df0f-413d-b9c8-5a06131fad91', '2025-12-01 16:09:37.431495+07', 'f', '2025-12-01 16:09:37.431495+07', '2025-12-01 17:09:37.429716+07', 'f', 'c2818fe1-3964-4662-917b-bfec1bc320b0', 'cddf6396-1973-4f50-8363-ebbf50b5bea6'),
    ('baf41448-9c0d-46a3-a4e9-dc848ea2e393', '2025-12-17 21:39:45.666221+07', 'f', '2025-12-17 21:39:45.666221+07', '2025-12-17 22:39:45.650793+07', 'f', '3e146f0a-a125-4e3f-a047-81ff72443078', '299f7701-a20b-49ff-adda-a748525cc7c0'),
    ('230f749a-67c5-4e5d-abc1-2ef61f053bb8', '2025-12-17 21:47:33.51445+07', 'f', '2025-12-17 21:47:33.51445+07', '2025-12-17 22:47:33.468901+07', 'f', '96d3507b-7ab9-4eb8-b973-87baad47f4fd', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 5210 (class 0 OID 26498)
-- Dependencies: 246
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5186 (class 0 OID 16946)
-- Dependencies: 222
-- Data for Name: post_comments; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: post_comments
INSERT INTO post_comments (comment_id, created_at, deleted, updated_at, content, status, parent_comment_id, post_id, user_id) VALUES
    ('0aacb11e-3497-4276-893b-6e62a19f557c', '2025-11-26 00:26:14.572469+07', 'f', '2025-11-26 00:26:14.572469+07', 'hrrr', NULL, NULL, '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', 'cddf6396-1973-4f50-8363-ebbf50b5bea6'),
    ('65ad9bb7-871a-4547-b93e-d757bf1bac1e', '2025-11-26 00:26:17.927454+07', 'f', '2025-11-26 00:26:17.927454+07', 'rrrr@HUY NGUYỄN THÀNH ', NULL, '0aacb11e-3497-4276-893b-6e62a19f557c', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', 'cddf6396-1973-4f50-8363-ebbf50b5bea6'),
    ('ed59bb1a-6896-4548-8a91-a065ef3dcf16', '2025-11-26 00:47:45.12823+07', 'f', '2025-11-26 00:47:45.12823+07', '@HUY NGUYỄN THÀNH  ??', NULL, '65ad9bb7-871a-4547-b93e-d757bf1bac1e', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('816e1d63-8f9b-4d8a-a5be-d9b2dd94924b', '2025-12-01 21:48:00.316553+07', 'f', '2025-12-01 21:48:00.316553+07', '@Nguyễn Thành Huy ', NULL, 'ed59bb1a-6896-4548-8a91-a065ef3dcf16', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('d2a96fde-0931-4b0d-94e3-99a53e73988c', '2025-12-06 01:58:15.393122+07', 'f', '2025-12-06 01:58:15.393122+07', 'hehe', NULL, '65ad9bb7-871a-4547-b93e-d757bf1bac1e', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('b07c3cd3-9cf0-483f-bc28-cd34f053cabc', '2025-12-06 02:00:18.639854+07', 'f', '2025-12-06 02:00:18.639854+07', 'hi', NULL, NULL, '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('3ec9ca0d-15a3-4d4b-be1f-2d1867208738', '2025-12-06 02:00:25.522721+07', 'f', '2025-12-06 02:00:25.522721+07', 'alo', NULL, 'b07c3cd3-9cf0-483f-bc28-cd34f053cabc', '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('a112f2e4-a8f7-41c3-875c-4408d3276699', '2025-12-06 02:00:36.196764+07', 'f', '2025-12-06 02:00:36.202409+07', 'hã', NULL, NULL, '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9');



--
-- TOC entry 5187 (class 0 OID 16953)
-- Dependencies: 223
-- Data for Name: post_likes; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: post_likes
INSERT INTO post_likes (like_id, created_at, deleted, updated_at, post_id, user_id) VALUES
    ('89976f9e-05f7-45a2-b4f1-4bf97c2ed3bc', '2025-11-26 00:26:09.395112+07', 'f', '2025-11-26 00:26:09.395112+07', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', 'cddf6396-1973-4f50-8363-ebbf50b5bea6'),
    ('ac03c557-725d-4d57-bdb2-fa275991c3d0', '2025-11-29 23:23:21.953831+07', 'f', '2025-11-29 23:23:21.953831+07', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('d4ef3bf7-b119-4b0d-ae66-72fe9d62cffe', '2025-11-26 00:47:26.406084+07', 'f', '2025-12-01 18:44:40.668078+07', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('4672ffd7-c991-44ba-a5e6-cbf552a3563a', '2025-11-26 00:25:40.056521+07', 't', '2025-12-03 23:21:19.847999+07', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('792aa228-69f9-4c4c-a4eb-7cb0290d69ef', '2025-12-01 15:15:35.911751+07', 'f', '2025-12-05 00:44:51.444322+07', 'ea75b418-0307-42b0-a3d4-872015982244', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('4c5a6c7c-1405-4e01-8add-c1b75de15b68', '2025-12-05 21:01:04.235707+07', 'f', '2025-12-06 01:02:34.456579+07', '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('b2172812-b6eb-4231-a1ad-8aa4403ab137', '2025-12-06 02:16:40.488786+07', 'f', '2025-12-06 02:16:40.488786+07', 'ea75b418-0307-42b0-a3d4-872015982244', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('8f0d9b1d-57e1-44c5-930d-d0bb51ee4f16', '2025-12-06 01:03:44.190064+07', 't', '2025-12-06 12:31:17.373415+07', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('a2cc8abf-a790-444d-b948-cfd167c4a30b', '2025-12-06 00:48:34.183008+07', 'f', '2025-12-06 12:31:19.015059+07', '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('82b33e39-6615-4e6f-aec2-2f6d6e0beedf', '2025-12-16 20:29:02.493771+07', 't', '2025-12-17 23:17:46.230927+07', '608c432c-a99c-4ba2-9cdd-d5c95c12a100', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 5188 (class 0 OID 16958)
-- Dependencies: 224
-- Data for Name: post_media; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: post_media
INSERT INTO post_media (post_media_id, created_at, deleted, updated_at, media_type, order_no, url, post_id) VALUES
    ('01bc871a-ee94-4a0f-afe2-7e4f06cb7ba4', '2025-11-26 00:24:35.561992+07', 'f', '2025-11-26 00:24:35.561992+07', 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764091477/viegym/bgefmfre04vx7utpzww8.webp', '5d4ef0b9-27d3-45c2-be06-ae3d5548ebd2'),
    ('ed67fc4b-9234-4a95-b967-0a45fd39d052', '2025-12-16 20:27:17.179047+07', 'f', '2025-12-16 20:27:17.179047+07', 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764855909/viegym/n6iczqvsyvvnwomtklj8.avif', '608c432c-a99c-4ba2-9cdd-d5c95c12a100'),
    ('a971e50e-93f6-4558-8bc0-e11ae0fdea93', '2025-12-18 04:52:29.871046+07', 'f', '2025-12-18 04:52:29.871046+07', 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764091255/viegym/czra91gymxfando8artn.webp', 'ea75b418-0307-42b0-a3d4-872015982244');



--
-- TOC entry 5203 (class 0 OID 26344)
-- Dependencies: 239
-- Data for Name: post_reports; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5189 (class 0 OID 16965)
-- Dependencies: 225
-- Data for Name: program_exercises; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: program_exercises
INSERT INTO program_exercises (program_exercise_id, created_at, deleted, updated_at, day_of_program, notes, order_no, reps, rest_seconds, sets, weight_scheme, exercise_id, program_id) VALUES
    ('d2950eae-c922-4762-8a2d-a44939a1c180', '2025-12-02 23:28:03.876518+07', 'f', '2025-12-02 23:28:03.876518+07', '2', '', '1', '10', '60', '3', NULL, 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', '978a3e9c-b5b4-4e6b-850e-c66c2853c728'),
    ('a68d3415-1123-4235-bca7-d46a0704c9b0', '2025-12-02 23:46:04.006236+07', 'f', '2025-12-02 23:46:04.006236+07', '1', '', '1', '20', '60', '3', NULL, 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '6786b3fe-1d0e-4a95-af92-7c5e770a380c'),
    ('3cdddcda-bb36-40c8-a862-9beb0df553a2', '2025-12-02 23:48:03.658786+07', 'f', '2025-12-02 23:48:03.658786+07', '1', '', '1', '20', '10', '2', NULL, 'c43c802d-d8bc-4d68-ad08-f05b3047d8bc', '8e6a5f3d-31e3-4b98-aea2-57caa0647707'),
    ('2949c170-3e7c-4730-930c-826ecb8dbccd', '2025-12-02 23:48:38.595401+07', 'f', '2025-12-02 23:48:38.595401+07', '5', '', '1', '10', '60', '3', NULL, '291050a3-939c-46a6-b71a-1db42e42da60', '8e6a5f3d-31e3-4b98-aea2-57caa0647707'),
    ('f815e3fc-8144-49de-8107-688e7ca55f3f', '2025-12-18 21:33:10.583767+07', 'f', '2025-12-18 21:33:10.583767+07', '1', '', '1', '10', '60', '3', NULL, '07f9990d-ac04-4066-a81e-4eb2b17e8628', '978a3e9c-b5b4-4e6b-850e-c66c2853c728'),
    ('3ff6a30d-e981-4a13-8fca-a1717405320d', '2025-12-18 21:33:31.093747+07', 'f', '2025-12-18 21:33:31.093747+07', '3', '', '1', '10', '60', '3', NULL, '54cf33f3-0b25-447c-be9e-b6a8043856ec', '978a3e9c-b5b4-4e6b-850e-c66c2853c728'),
    ('89f20f81-997b-4f33-8103-cd58f2d50a25', '2025-12-18 21:33:42.135995+07', 'f', '2025-12-18 21:33:42.135995+07', '4', '', '1', '10', '60', '3', NULL, 'ccabd244-1416-416b-9cd1-b09aa817787e', '978a3e9c-b5b4-4e6b-850e-c66c2853c728'),
    ('85be6deb-6597-4960-9fda-971223e79402', '2025-12-18 21:34:32.685437+07', 'f', '2025-12-18 21:34:32.685437+07', '1', '', '1', '10', '60', '3', NULL, '54cf33f3-0b25-447c-be9e-b6a8043856ec', 'b01b570f-969d-4107-b0e1-926392c91a54'),
    ('e61f73c5-a91e-42e0-9da3-97f024451380', '2025-12-18 21:34:36.670607+07', 'f', '2025-12-18 21:34:36.671604+07', '1', '', '1', '10', '60', '3', NULL, 'cbf9614d-2054-4589-b3a0-d654afdf9a62', 'b01b570f-969d-4107-b0e1-926392c91a54'),
    ('71afda0a-2606-4edf-b7e9-01d1b47ec383', '2025-12-18 21:34:43.144235+07', 'f', '2025-12-18 21:34:43.144235+07', '1', '', '1', '10', '60', '3', NULL, 'dd7fa767-c813-4134-99e6-1af95257bf91', 'd8bcb73c-534d-424a-b6b9-0bfd0a3ee965'),
    ('71a2d2c1-ed35-4a34-ae8d-303deacfdb94', '2025-12-18 21:34:48.544039+07', 'f', '2025-12-18 21:34:48.544039+07', '1', '', '1', '10', '60', '3', NULL, 'd851ba0e-eb60-4e75-8ff4-ca29244efca7', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4'),
    ('84996b82-da29-44c5-89b0-06f82b9adc32', '2025-12-18 21:34:51.822724+07', 'f', '2025-12-18 21:34:51.822724+07', '1', '', '1', '10', '60', '3', NULL, 'f8f8187b-6f43-4732-af56-edf961ac16da', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4'),
    ('48ce0a12-490d-4c2a-8088-10c08121babf', '2025-12-18 21:34:58.603258+07', 'f', '2025-12-18 21:34:58.603258+07', '1', '', '1', '10', '60', '3', NULL, '889716ea-a96b-4222-88d5-510a6e2655fd', '8e6a5f3d-31e3-4b98-aea2-57caa0647707');



--
-- TOC entry 5199 (class 0 OID 18053)
-- Dependencies: 235
-- Data for Name: program_media; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: program_media
INSERT INTO program_media (id, created_at, deleted, updated_at, caption, media_type, order_no, url, program_id) VALUES
    ('3550cba2-692e-4725-842e-e60ff5da3fcc', '2025-12-02 23:25:57.561797+07', 'f', '2025-12-02 23:25:57.561797+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764692742/viegym/x680vzbwvsm2chxqvh8m.webp', '978a3e9c-b5b4-4e6b-850e-c66c2853c728'),
    ('ea5ef307-0841-42d6-ae1d-42a4c07be6b4', '2025-12-02 23:45:40.730396+07', 'f', '2025-12-02 23:45:40.730396+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764693898/viegym/udzfbku8lhnbnhxtfgcm.webp', '6786b3fe-1d0e-4a95-af92-7c5e770a380c'),
    ('8ff088c6-5186-4faf-88d6-3a5c8d5aa2f1', '2025-12-02 23:47:44.815991+07', 'f', '2025-12-02 23:47:44.815991+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764694047/viegym/oqsgtdshxaox2hzspsvk.webp', '8e6a5f3d-31e3-4b98-aea2-57caa0647707'),
    ('d1c69344-da8c-483b-b41b-2df80dfbd3ea', '2025-12-02 23:49:36.54279+07', 'f', '2025-12-02 23:49:36.54279+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764694167/viegym/r94aa1kler2oetku551l.jpg', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4'),
    ('9d9963d6-8dd2-4bc3-ae64-81e5c12d170d', '2025-12-02 23:50:13.119088+07', 'f', '2025-12-02 23:50:13.119088+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764694204/viegym/gttkdpfuauk27hpi2nio.jpg', 'd8bcb73c-534d-424a-b6b9-0bfd0a3ee965'),
    ('14b1b2a8-a89d-4e32-a1d5-a6359417d9ca', '2025-12-02 23:50:53.245457+07', 'f', '2025-12-02 23:50:53.245457+07', NULL, 'IMAGE', NULL, 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764694243/viegym/z1toozl7ehphwdftkhcf.webp', 'b01b570f-969d-4107-b0e1-926392c91a54');



--
-- TOC entry 5201 (class 0 OID 26305)
-- Dependencies: 237
-- Data for Name: program_ratings; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: program_ratings
INSERT INTO program_ratings (rating_id, created_at, deleted, updated_at, rating, review, program_id, user_id) VALUES
    ('b2fabe22-aac0-44f8-be4d-ad455944647a', '2025-12-02 23:59:58.116541+07', 'f', '2025-12-02 23:59:58.116541+07', '5', '', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('f92ded72-e216-4516-a552-d5393cd0d2e0', '2025-12-13 22:39:10.474285+07', 'f', '2025-12-13 22:39:31.915406+07', '5', '', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 5190 (class 0 OID 16972)
-- Dependencies: 226
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: refresh_tokens
INSERT INTO refresh_tokens (token_id, created_at, deleted, updated_at, expiry_date, is_revoked, refresh_token, user_id) VALUES
    ('0d01328c-3141-4905-af14-6783f2489e91', '2025-12-18 21:44:33.671981+07', 'f', '2025-12-18 21:44:33.671981+07', '2025-12-25 21:44:33.671981+07', 'f', '3d392e1c-8045-478d-a860-298dade7a32a', '0e9ff875-6fa7-4145-9b43-e38c6e7dac6f');



--
-- TOC entry 5191 (class 0 OID 16977)
-- Dependencies: 227
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: roles
INSERT INTO roles (role_id, created_at, deleted, updated_at, name) VALUES
    ('1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '2025-09-29 22:41:30.800438+07', 'f', '2025-09-29 22:41:30.800438+07', 'ROLE_USER'),
    ('982a5fbc-d16b-4704-a0db-a5a8581fbc85', '2025-09-29 22:41:30.835417+07', 'f', '2025-09-29 22:41:30.835417+07', 'ROLE_ADMIN'),
    ('791e4324-605a-45c5-b335-8c70ce44e1fa', '2025-09-29 22:41:30.844297+07', 'f', '2025-09-29 22:41:30.844297+07', 'ROLE_COACH'),
    ('0c7ed0b8-19d3-4f65-8758-e78de6d38590', '2025-12-02 20:31:59.553598+07', 'f', '2025-12-02 20:31:59.553598+07', 'ROLE_SUPER_ADMIN');



--
-- TOC entry 5202 (class 0 OID 26312)
-- Dependencies: 238
-- Data for Name: saved_programs; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: saved_programs
INSERT INTO saved_programs (saved_id, created_at, deleted, updated_at, program_id, user_id) VALUES
    ('bed8f040-4698-4314-bd5f-96b9808c3367', '2025-12-18 03:04:22.398066+07', 'f', '2025-12-18 03:04:22.398066+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('558735b6-4ef8-4214-a6d2-841510483548', '2025-12-18 03:33:36.219654+07', 'f', '2025-12-18 03:33:36.219654+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('769800d2-4373-4540-a32f-42735e957fb6', '2025-12-18 03:36:04.519607+07', 'f', '2025-12-18 03:36:04.519607+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('ff6cbd9a-6d37-4f9a-a804-09a87a97d845', '2025-12-18 03:38:45.574465+07', 'f', '2025-12-18 03:38:45.574465+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('96c8d3ff-fca3-4a07-b774-e8bb3f69541c', '2025-12-18 04:30:38.309706+07', 'f', '2025-12-18 04:30:38.309706+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('46cd44b9-670a-4e5f-8a17-e8591e6dd0d7', '2025-12-18 21:43:20.067043+07', 'f', '2025-12-18 21:43:20.067043+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9');



--
-- TOC entry 5192 (class 0 OID 16983)
-- Dependencies: 228
-- Data for Name: session_exercise_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: session_exercise_logs
INSERT INTO session_exercise_logs (log_id, created_at, reps_done, set_number, weight_used, exercise_id, session_id, body_weight, distance_meters, duration_seconds, set_notes, updated_at, deleted, completed) VALUES
    ('500c6199-4462-4e19-b2be-94bbf1345d2b', '2025-11-29 23:46:39.220389+07', '10', '1', '0', 'ccabd244-1416-416b-9cd1-b09aa817787e', 'ff86c86f-4e84-4a5b-af74-4800cc01852f', NULL, NULL, NULL, NULL, NULL, 'f', 'f'),
    ('267b7de3-df46-4a99-b07e-789a48895743', '2025-12-05 01:16:45.790685+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', 'f66194be-4d15-4761-bf78-125f1fa8eeb0', NULL, NULL, '0', NULL, '2025-12-05 01:16:45.790685+07', 'f', 'f'),
    ('365d34af-b487-4f49-ba4d-35da982bffe8', '2025-12-05 01:16:45.809391+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', 'f66194be-4d15-4761-bf78-125f1fa8eeb0', NULL, NULL, '0', NULL, '2025-12-05 01:16:45.809391+07', 'f', 'f'),
    ('3d65c547-0855-4bd3-ab9f-8d83b80605a3', '2025-12-05 01:16:45.824174+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', 'f66194be-4d15-4761-bf78-125f1fa8eeb0', NULL, NULL, '0', NULL, '2025-12-05 01:16:45.824174+07', 'f', 'f'),
    ('0cfe34fe-061e-441c-a926-eee4cd775bb2', '2025-12-05 01:20:06.928059+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '93e86ab8-191f-4377-ac32-edda475895c8', NULL, NULL, NULL, NULL, '2025-12-05 01:20:06.928059+07', 'f', 'f'),
    ('e9d29379-7a51-460d-8b2e-8fb5a12f2760', '2025-12-05 01:20:06.963246+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '93e86ab8-191f-4377-ac32-edda475895c8', NULL, NULL, NULL, NULL, '2025-12-05 01:20:06.963246+07', 'f', 'f'),
    ('f30efe0f-4c81-4ae3-9d40-a9356762be33', '2025-12-05 01:20:07.002357+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '93e86ab8-191f-4377-ac32-edda475895c8', NULL, NULL, NULL, NULL, '2025-12-05 01:20:07.002357+07', 'f', 'f'),
    ('8b67ecb6-672c-4804-b4e1-efdfce0d4630', '2025-12-05 01:21:31.830839+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '1ae82999-508c-422b-b399-f667c7500cfd', NULL, NULL, NULL, NULL, '2025-12-05 01:21:31.830839+07', 'f', 'f'),
    ('88f96d73-59b6-47e5-81ed-82dfe452ae1b', '2025-12-05 01:21:31.854771+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '1ae82999-508c-422b-b399-f667c7500cfd', NULL, NULL, NULL, NULL, '2025-12-05 01:21:31.854771+07', 'f', 'f'),
    ('40c8b59d-0c20-4fc0-bc51-a7e47e7b8c03', '2025-12-05 01:21:31.879552+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '1ae82999-508c-422b-b399-f667c7500cfd', NULL, NULL, NULL, NULL, '2025-12-05 01:21:31.879552+07', 'f', 'f'),
    ('37992ddd-4cb1-4585-a176-3cd39bf1d750', '2025-12-05 01:23:11.799177+07', '10', '1', '0', 'f8f8187b-6f43-4732-af56-edf961ac16da', '1ae82999-508c-422b-b399-f667c7500cfd', '70', '100', '60', NULL, '2025-12-05 01:23:11.799177+07', 'f', 'f'),
    ('92d66816-ad4b-4ac1-ab51-48ef9144dfe2', '2025-12-05 20:55:01.974867+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '09495ad3-86b2-4b8e-83d5-5e0609ae7ee8', NULL, NULL, NULL, NULL, '2025-12-05 20:55:01.974867+07', 'f', 'f'),
    ('d68838fb-8ee9-4eb0-98dc-cb4903d32710', '2025-12-05 20:55:01.995651+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '09495ad3-86b2-4b8e-83d5-5e0609ae7ee8', NULL, NULL, NULL, NULL, '2025-12-05 20:55:01.995651+07', 'f', 'f'),
    ('9e723632-eed0-433a-bbb1-abbf4eb96392', '2025-12-05 20:55:02.009476+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '09495ad3-86b2-4b8e-83d5-5e0609ae7ee8', NULL, NULL, NULL, NULL, '2025-12-05 20:55:02.009476+07', 'f', 'f'),
    ('a0cf7bb1-9bdf-4ec4-9610-9da7cb83c307', '2025-12-12 23:07:44.540231+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '462560d0-2547-4371-8246-08237bbbc291', NULL, NULL, NULL, NULL, '2025-12-12 23:07:44.540231+07', 'f', 'f'),
    ('381232d9-2fd7-4bd9-af45-b72893d42395', '2025-12-12 23:07:44.570304+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '462560d0-2547-4371-8246-08237bbbc291', NULL, NULL, NULL, NULL, '2025-12-12 23:07:44.570304+07', 'f', 'f'),
    ('6f8b0586-e0ee-494c-b373-30e482ccc99f', '2025-12-12 23:07:44.600717+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '462560d0-2547-4371-8246-08237bbbc291', NULL, NULL, NULL, NULL, '2025-12-12 23:07:44.600717+07', 'f', 'f'),
    ('1e156a53-378a-43c2-8ca4-52b321ad7bd3', '2025-12-12 23:07:57.372785+07', '10', '1', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', 'b296f71c-b8ce-4ae3-81c4-406e69a75bc0', NULL, NULL, NULL, NULL, '2025-12-12 23:07:57.372785+07', 'f', 'f'),
    ('339414ea-2da0-409d-8732-f5b6aae2b105', '2025-12-12 23:07:57.389784+07', '10', '2', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', 'b296f71c-b8ce-4ae3-81c4-406e69a75bc0', NULL, NULL, NULL, NULL, '2025-12-12 23:07:57.389784+07', 'f', 'f'),
    ('27c750f6-d004-43ba-9700-b3c1f3dcc177', '2025-12-12 23:07:57.410394+07', '10', '3', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', 'b296f71c-b8ce-4ae3-81c4-406e69a75bc0', NULL, NULL, NULL, NULL, '2025-12-12 23:07:57.410394+07', 'f', 'f'),
    ('2ac3e858-fc63-4e15-b767-0baaa3d813f1', '2025-12-13 22:40:13.139243+07', '20', '1', '0', 'c43c802d-d8bc-4d68-ad08-f05b3047d8bc', '15df5b40-3ba9-4acc-9f5c-4151097b8fb9', NULL, NULL, NULL, NULL, '2025-12-13 22:40:13.139243+07', 'f', 'f'),
    ('c790199e-931a-4ddb-a9f6-d2ed4bcfd942', '2025-12-13 22:40:13.157724+07', '20', '2', '0', 'c43c802d-d8bc-4d68-ad08-f05b3047d8bc', '15df5b40-3ba9-4acc-9f5c-4151097b8fb9', NULL, NULL, NULL, NULL, '2025-12-13 22:40:13.157724+07', 'f', 'f'),
    ('04d8efd4-bfe9-4cda-a4ba-15c4b6060b0d', '2025-12-13 22:40:13.322185+07', '10', '1', '0', '291050a3-939c-46a6-b71a-1db42e42da60', '15df5b40-3ba9-4acc-9f5c-4151097b8fb9', NULL, NULL, NULL, NULL, '2025-12-13 22:40:13.322185+07', 'f', 'f'),
    ('048d234c-d97e-4853-ad21-ef7bc73ce1c9', '2025-12-13 22:40:13.34095+07', '10', '2', '0', '291050a3-939c-46a6-b71a-1db42e42da60', '15df5b40-3ba9-4acc-9f5c-4151097b8fb9', NULL, NULL, NULL, NULL, '2025-12-13 22:40:13.34095+07', 'f', 'f'),
    ('c84d9ece-78ac-41aa-80cc-975cfd922386', '2025-12-13 22:40:13.359354+07', '10', '3', '0', '291050a3-939c-46a6-b71a-1db42e42da60', '15df5b40-3ba9-4acc-9f5c-4151097b8fb9', NULL, NULL, NULL, NULL, '2025-12-13 22:40:13.359354+07', 'f', 'f'),
    ('a233e793-2cbf-4940-8670-9bd0b098a3ec', '2025-12-18 01:11:43.205388+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '152c2615-1f55-40f7-b60c-afc4a0d060c8', NULL, NULL, NULL, NULL, '2025-12-18 01:11:43.205388+07', 'f', 'f'),
    ('d93f5281-0a7b-4122-8649-0627131530f8', '2025-12-18 01:11:43.22624+07', '20', '2', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '152c2615-1f55-40f7-b60c-afc4a0d060c8', NULL, NULL, NULL, NULL, '2025-12-18 01:11:43.22624+07', 'f', 'f'),
    ('40e0384a-241c-42f9-b732-5ae283d0ac01', '2025-12-18 01:11:43.245003+07', '20', '3', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '152c2615-1f55-40f7-b60c-afc4a0d060c8', NULL, NULL, NULL, NULL, '2025-12-18 01:11:43.245003+07', 'f', 'f'),
    ('1b8dfc08-8250-472c-bb4c-73876a9b5d9c', '2025-12-18 01:25:46.670371+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '3320221f-4915-405b-bbdf-dddefb7a5d88', NULL, NULL, NULL, NULL, '2025-12-18 01:25:46.670371+07', 'f', 'f'),
    ('da984219-1eba-4b18-8367-e622dfbb141c', '2025-12-18 01:28:35.311772+07', '10', '1', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', '3cc67078-551c-4bb8-8802-9a88680044a0', NULL, NULL, NULL, NULL, '2025-12-18 01:28:35.311772+07', 'f', 'f'),
    ('a0cb0d16-f933-4cdd-b576-36774ea4def6', '2025-12-18 01:28:45.485646+07', '10', '2', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', '3cc67078-551c-4bb8-8802-9a88680044a0', NULL, NULL, NULL, NULL, '2025-12-18 01:28:45.485646+07', 'f', 'f'),
    ('21916d87-9b66-4628-a176-7e545761108a', '2025-12-18 01:28:47.976184+07', '10', '3', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', '3cc67078-551c-4bb8-8802-9a88680044a0', NULL, NULL, NULL, NULL, '2025-12-18 01:28:47.976184+07', 'f', 'f'),
    ('a7cc8322-1f1e-4ee2-ab5d-6147659c0acf', '2025-12-18 01:28:48.448369+07', '10', '4', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', '3cc67078-551c-4bb8-8802-9a88680044a0', NULL, NULL, NULL, NULL, '2025-12-18 01:28:48.448369+07', 'f', 'f'),
    ('f2ccc36c-9eba-473a-aa02-9f53b47e25eb', '2025-12-18 02:04:06.506421+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '115cc836-ddd6-4ca1-a0fc-e2d9fbda1d1f', NULL, NULL, NULL, NULL, '2025-12-18 02:04:06.506421+07', 'f', 'f'),
    ('e3de9bb9-4018-460b-88a0-11425be7d1e7', '2025-12-18 02:16:29.372637+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', 'e4420abf-9939-4ee3-909a-cc3b868a509b', NULL, NULL, NULL, NULL, '2025-12-18 02:16:29.372637+07', 'f', 'f'),
    ('e7738d9c-a7df-4796-976e-f6712583458a', '2025-12-18 02:25:22.062916+07', '20', '1', '0', 'b8db462d-a035-4ee1-bdb9-e87e3feb3629', '8f99d9ae-c6c9-4494-a9e8-5ea8b926147d', NULL, NULL, NULL, NULL, '2025-12-18 02:25:22.062916+07', 'f', 'f'),
    ('2b7385d0-261a-4063-ad60-f2aade1b232c', '2025-12-18 02:25:44.946851+07', NULL, '1', NULL, 'f8f8187b-6f43-4732-af56-edf961ac16da', '8f99d9ae-c6c9-4494-a9e8-5ea8b926147d', NULL, '100', NULL, NULL, '2025-12-18 02:25:44.946851+07', 'f', 'f'),
    ('83fb91b1-0827-4ba6-86b8-afa0b6ba33cf', '2025-12-18 02:59:07.803511+07', '10', '1', '0', 'b17938e9-2d3f-4d54-a826-0e44f3d074eb', 'dbb628c9-c2f7-4e6a-b00e-29df26a8bc06', NULL, NULL, NULL, NULL, '2025-12-18 03:04:36.39235+07', 'f', 'f'),
    ('8d124ab9-d787-4840-a34c-d6362bf8dc1e', '2025-12-18 02:35:38.292189+07', '20', '1', '0', 'c43c802d-d8bc-4d68-ad08-f05b3047d8bc', 'f7395435-3eb4-44ca-a778-54c51ac1d14c', NULL, NULL, NULL, NULL, '2025-12-18 02:35:39.702418+07', 'f', 't'),
    ('62c82a5f-001b-4966-8192-c24dc163f7c2', '2025-12-18 02:35:38.368659+07', '10', '1', '0', '291050a3-939c-46a6-b71a-1db42e42da60', 'f7395435-3eb4-44ca-a778-54c51ac1d14c', NULL, NULL, NULL, NULL, '2025-12-18 02:35:44.535748+07', 'f', 't'),
    ('49e7c3e3-1296-4d17-89f8-adb6e1d2db99', '2025-12-18 02:36:58.047605+07', '10', '1', '3', 'b602c0d3-d757-4fb5-bd44-b484e5da6fa8', 'f7395435-3eb4-44ca-a778-54c51ac1d14c', NULL, NULL, NULL, NULL, '2025-12-18 02:37:21.922116+07', 'f', 't');



--
-- TOC entry 5198 (class 0 OID 17159)
-- Dependencies: 234
-- Data for Name: tags; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: tags
INSERT INTO tags (tag_id, name, created_at, deleted, updated_at) VALUES
    ('000f6bdf-933d-4650-acf0-2586c8874ab7', 'strength', '2025-10-03 14:39:38.248408+07', 'f', '2025-10-03 14:39:38.248408+07'),
    ('5b41909e-43e7-4bf0-995f-48576de1e9b0', 'legs', '2025-10-03 14:39:38.256508+07', 'f', '2025-10-03 14:39:38.256508+07'),
    ('2b086bd4-8e06-4dfe-b1db-1a63452f43d9', 'chest', '2025-10-03 15:44:39.0191+07', 'f', '2025-10-03 15:44:39.0191+07'),
    ('ddd6d8ea-f7e0-40a5-9398-434be728c8d2', 'compound', '2025-10-03 15:44:39.028618+07', 'f', '2025-10-03 15:44:39.028618+07'),
    ('a2fb14e7-bb95-42c5-8616-ea48cfa3164b', 'bodyweight', '2025-10-03 15:45:02.406999+07', 'f', '2025-10-03 15:45:02.406999+07'),
    ('d2581be8-ba4a-4358-8710-9802953b3116', 'back', '2025-10-03 15:45:02.414501+07', 'f', '2025-10-03 15:45:02.414501+07'),
    ('3da1c204-6783-40d4-993d-2648678b478f', 'shoulders', '2025-10-03 15:45:22.677819+07', 'f', '2025-10-03 15:45:22.678827+07'),
    ('44e0502f-991c-4a0b-94c4-3117d1444c74', 'tạ', '2025-11-22 14:07:43.501279+07', 'f', '2025-11-22 14:07:43.501279+07'),
    ('78936936-accc-4bc0-9c53-8c1b946c8815', 'Tay', '2025-12-02 21:15:47.053172+07', 'f', '2025-12-02 21:15:47.053172+07'),
    ('99a23326-2ec1-4975-80cf-f2f9b194bdaa', 'lưng', '2025-12-18 21:14:35.693534+07', 'f', '2025-12-18 21:14:35.693534+07'),
    ('03a670c9-8af4-4abc-9f10-550ded0a57cd', 'ngực', '2025-12-18 21:14:49.443907+07', 'f', '2025-12-18 21:14:49.443907+07');



--
-- TOC entry 5193 (class 0 OID 16988)
-- Dependencies: 229
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: user_roles
INSERT INTO user_roles (user_role_id, created_at, deleted, updated_at, assigned_by, role_id, user_id) VALUES
    ('19085b0b-b72f-48d8-b136-13c0b1a9b135', '2025-11-25 22:44:29.920316+07', 'f', '2025-11-25 22:44:29.920316+07', NULL, '791e4324-605a-45c5-b335-8c70ce44e1fa', '5d2e8dec-8812-4d73-8dc3-99bd366e9be9'),
    ('ab5e3dd1-51cf-40f0-8d4d-273842f30e14', '2025-11-25 22:44:29.920316+07', 'f', '2025-11-25 22:44:29.920316+07', NULL, '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', 'c8c83b9d-ef94-43d0-8e47-0d923ca7859a'),
    ('3fa652b4-64bd-4c3b-97d5-ef237183770b', '2025-11-25 22:44:29.920316+07', 'f', '2025-11-25 22:44:29.920316+07', NULL, '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '7e3fb8b4-8f03-46ce-8e28-c4c537dfc81d'),
    ('4b650670-c078-4d07-88cd-c515ad8dc59e', '2025-11-25 22:44:29.920316+07', 'f', '2025-11-25 22:44:29.920316+07', NULL, '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', 'f8b36b43-2ea6-4243-a406-f7ba51f302d5'),
    ('2e94e06e-0147-4cc8-8321-335fa8982038', '2025-11-26 00:20:43.504628+07', 'f', '2025-11-26 00:20:43.504628+07', '8a79865b-85ed-4487-8a77-c560bb853ede', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('654b6cef-d8d8-4f53-9bd5-dc47835a77d7', '2025-11-26 00:24:04.495049+07', 'f', '2025-11-26 00:24:04.496043+07', '299f7701-a20b-49ff-adda-a748525cc7c0', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '299f7701-a20b-49ff-adda-a748525cc7c0'),
    ('a7658804-2d6b-43be-a5af-7f78d358236d', '2025-11-29 23:23:13.847765+07', 'f', '2025-11-29 23:23:13.847765+07', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('17758342-e416-4cfd-a6bf-88db905f253e', '2025-11-30 13:16:52.165165+07', 'f', '2025-11-30 13:16:52.165165+07', '10f1b635-e796-4c9d-aa36-aeef8cd25990', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '10f1b635-e796-4c9d-aa36-aeef8cd25990'),
    ('c5f6f160-a8d7-4790-9202-bd37d07c1515', '2025-12-01 16:33:02.089736+07', 'f', '2025-12-01 16:33:02.089736+07', '37281177-8b54-45ad-bf37-568deb8e33cd', '982a5fbc-d16b-4704-a0db-a5a8581fbc85', 'cddf6396-1973-4f50-8363-ebbf50b5bea6'),
    ('000b9906-faa8-49fd-bb6b-ac04bfd8b686', '2025-12-02 20:31:59.660532+07', 'f', '2025-12-02 20:31:59.660532+07', '0e9ff875-6fa7-4145-9b43-e38c6e7dac6f', '0c7ed0b8-19d3-4f65-8758-e78de6d38590', '0e9ff875-6fa7-4145-9b43-e38c6e7dac6f'),
    ('a1cfc67d-395b-4d35-800f-48306c93f09c', '2025-12-02 22:23:11.220618+07', 'f', '2025-12-02 22:23:11.220618+07', '0e9ff875-6fa7-4145-9b43-e38c6e7dac6f', '982a5fbc-d16b-4704-a0db-a5a8581fbc85', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('9d8efbee-caec-473c-a085-8af578117cc6', '2025-12-10 00:34:51.590223+07', 'f', '2025-12-10 00:34:51.590223+07', '7c2ab5e3-4655-437c-ab37-7be2d3097842', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', '7c2ab5e3-4655-437c-ab37-7be2d3097842'),
    ('12d72c37-c185-48d6-9a11-a26bc2db3a95', '2025-12-17 23:14:45.898743+07', 'f', '2025-12-17 23:14:45.898743+07', 'a3e8ba85-5ee3-430c-a101-18547185180c', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', 'a3e8ba85-5ee3-430c-a101-18547185180c'),
    ('28142e99-181f-4478-a024-b06d6232c9d0', '2025-12-18 03:33:21.013462+07', 'f', '2025-12-18 03:33:21.013462+07', 'e50f699c-fad5-4b88-80cf-613145fa27e6', '1b5973b3-cd6a-47ac-8b06-f93bdb206c3e', 'e50f699c-fad5-4b88-80cf-613145fa27e6');



--
-- TOC entry 5194 (class 0 OID 16993)
-- Dependencies: 230
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: users
INSERT INTO users (user_id, created_at, deleted, updated_at, birth_date, body_fat_percent, email, experience_level, full_name, gender, goal, height_cm, password, weight_kg, provider, avatar_url, last_login, status, phone, daily_calorie_goal, daily_water_goal, daily_workout_mins, dark_mode, language, notifications, streak_days, total_workouts, total_volume, last_streak_update) VALUES
    ('7c2ab5e3-4655-437c-ab37-7be2d3097842', '2025-12-10 00:34:51.585744+07', 'f', '2025-12-18 21:01:15.715623+07', NULL, NULL, 'ngthanhhuy09104@gmail.com', NULL, 'Nguyễn Thành Long', NULL, NULL, NULL, '$2a$10$V22Ht33NTSjJsz3ErjDl.up3JaiqiFC0B0QFSlnKqlSm.wzz9N9Cu', NULL, 'GOOGLE', NULL, '2025-12-10 00:34:51.583217+07', 'ACTIVE', '0335233412', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', NULL),
    ('299f7701-a20b-49ff-adda-a748525cc7c0', '2025-11-26 00:24:04.493043+07', 'f', '2025-12-18 21:03:29.380203+07', NULL, NULL, 'thanhyuh0910@gmail.com', NULL, 'Trần Kim Toàn', NULL, NULL, NULL, '$2a$10$mzeH7n1IuwuCXLFwL7fl1e7SQWzYheOZpHPOfvXm0lIEpg93DJGsO', NULL, 'GOOGLE', NULL, '2025-12-16 19:07:25.513231+07', 'ACTIVE', '0335222126', NULL, NULL, NULL, NULL, NULL, NULL, '0', '1', '0', NULL),
    ('a3e8ba85-5ee3-430c-a101-18547185180c', '2025-12-17 23:14:45.893118+07', 'f', '2025-12-18 21:04:02.235932+07', NULL, NULL, 'huy@gmail.com', NULL, 'Tràn Đạt', NULL, NULL, NULL, '$2a$10$NFA/XW/YqO4W.6Xwwt9U8etlKweYSMNKCsJPCrFujNfqCu7B2cXz6', NULL, NULL, NULL, '2025-12-17 23:14:58.734476+07', 'ACTIVE', '0335233744', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', NULL),
    ('fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee', '2025-11-29 23:23:13.837739+07', 'f', '2025-12-18 21:04:15.441518+07', NULL, NULL, 'huytnguyen2004@gmail.com', NULL, 'Thanh Bình', NULL, NULL, NULL, '$2a$10$bc7rwlNBM55krGPYqGIV9uiGxxpyqirfsR2n9TZzuqbfsW.NKhm3S', NULL, 'GOOGLE', NULL, '2025-12-12 23:47:10.173381+07', 'ACTIVE', '0335233763', NULL, NULL, NULL, NULL, NULL, NULL, '1', '1', '0', '2025-11-30'),
    ('cddf6396-1973-4f50-8363-ebbf50b5bea6', '2025-11-26 00:26:01.500532+07', 'f', '2025-12-18 21:04:23.576045+07', NULL, NULL, '2200011141@nttu.edu.vn', NULL, 'Nguyễn Thành', NULL, NULL, NULL, '$2a$10$Mm5FcScTk8vbhRFFQGIp4.k7wMgYSEr4IhDsFUVHd4KVKXp2nomJ2', NULL, 'GOOGLE', NULL, '2025-12-17 22:24:25.090786+07', 'ACTIVE', '0335233759', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', NULL),
    ('10f1b635-e796-4c9d-aa36-aeef8cd25990', '2025-11-30 13:16:52.160802+07', 'f', '2025-12-18 21:04:30.145687+07', NULL, NULL, 'huy.nt@geekup.vn', NULL, 'Huy Thành', NULL, NULL, NULL, '$2a$10$YniYkAenRobxHa8ZgXAtP.Wdmw2Eicu9GkgL7Kw0FbYdGO.jBjIEu', NULL, 'GOOGLE', NULL, NULL, 'ACTIVE', '0335233418', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', NULL),
    ('8a79865b-85ed-4487-8a77-c560bb853ede', '2025-11-26 00:20:43.497969+07', 'f', '2025-12-18 21:35:13.753883+07', '2004-01-09', '1', 'huymixi2k4@gmail.com', 'BEGINNER', 'Huy Hoàng', 'MALE', 'BUILD_MUSCLE', '180', '$2a$10$P6bclZ2D7ZHGYq.h4ZQvIuxYuC4xaFCY7.vPacKUhxIOLGqcuKR/i', '58', 'GOOGLE', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1766064136/viegym/rqtiioikoha9nxachg7z.webp', '2025-12-18 21:35:13.752363+07', 'ACTIVE', '0123456789', '2', '4', '60', 'f', NULL, 'f', '1', '31', '29210', '2025-12-18'),
    ('0e9ff875-6fa7-4145-9b43-e38c6e7dac6f', '2025-12-02 20:31:59.656422+07', 'f', '2025-12-18 21:44:33.66801+07', NULL, NULL, 'huyadmin@gmail.com', NULL, 'Thành Huy', NULL, NULL, NULL, '$2a$10$LdtGMgWSeDJUneY/MIS7ru04kHXVm0iWaHoGNsmbJqeOjCjFnmJA.', NULL, NULL, NULL, '2025-12-18 21:44:33.66801+07', 'ACTIVE', '0335233757', NULL, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', NULL),
    ('e50f699c-fad5-4b88-80cf-613145fa27e6', '2025-12-18 03:33:21.008554+07', 'f', '2025-12-18 21:04:39.179417+07', NULL, NULL, 'huynt2k4zz@gmail.com', NULL, 'Huy Thành', NULL, NULL, NULL, '$2a$10$bsWrPQ02Y4eJhwz51yuNMOG0q.6yvmzICUeuDgMKCQe12b0SEQk06', NULL, 'GOOGLE', NULL, '2025-12-18 03:33:21.005985+07', 'ACTIVE', '0335233751', NULL, NULL, NULL, NULL, NULL, NULL, '1', '5', '0', '2025-12-18'),
    ('37281177-8b54-45ad-bf37-568deb8e33cd', '2025-11-25 22:44:25.774259+07', 'f', '2025-12-18 21:21:19.526233+07', '2004-10-09', '3', 'admin@viegym.com', 'Cao cấp', 'Nguyễn Thành Huy', 'Nam', 'TĂNG CƠ', '172', '$2a$10$fAte0TMT/RxmyZ/snGl4qOzJYvejJBx3saRqdbgEqFx6Tamt88vm6', '51.5', 'LOCAL', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764954698/viegym/mudec4rrrg9dbmybtvrb.jpg', '2025-12-18 21:21:19.514241+07', 'ACTIVE', '0335233757', '2500', '10', '90', 'f', 'vi', 't', '0', '135', '20600', NULL),
    ('5d2e8dec-8812-4d73-8dc3-99bd366e9be9', '2025-11-25 22:44:25.774259+07', 'f', '2025-12-18 21:35:45.774264+07', '1988-05-20', '18', 'coach@viegym.com', 'Chuyên gia', 'Trần Văn Huấn', 'Nữ', 'DUY TRÌ SỨC KHỎE', '165', '$2a$10$fAte0TMT/RxmyZ/snGl4qOzJYvejJBx3saRqdbgEqFx6Tamt88vm6', '55', 'LOCAL', 'https://res.cloudinary.com/dfhlxbncn/image/upload/v1764962652/viegym/ykoyixmwxnbuwfkoe6k9.png', '2025-12-18 21:35:45.774265+07', 'ACTIVE', '0912345678', '0', '8', '60', 'f', 'vi', 't', '0', '200', '25000', NULL),
    ('7e3fb8b4-8f03-46ce-8e28-c4c537dfc81d', '2025-11-25 22:44:25.774259+07', 'f', '2025-11-25 22:49:38.212074+07', '2000-03-25', '22', 'user2@viegym.com', 'Sơ cấp', 'Phạm Thị Bình', 'Nữ', 'TĂNG CƠ', '160', '$2a$10$fAte0TMT/RxmyZ/snGl4qOzJYvejJBx3saRqdbgEqFx6Tamt88vm6', '52', 'LOCAL', 'https://i.pravatar.cc/150?img=20', NULL, 'ACTIVE', '0934567890', '1600', '8', '30', 'f', 'vi', 't', '7', '15', '2000', '2025-11-25'),
    ('f8b36b43-2ea6-4243-a406-f7ba51f302d5', '2025-11-25 22:44:25.774259+07', 'f', '2025-11-25 22:49:38.212074+07', '1992-11-08', '12', 'user3@viegym.com', 'Cao cấp', 'Hoàng Minh Cường', 'Nam', 'TĂNG CƠ', '178', '$2a$10$fAte0TMT/RxmyZ/snGl4qOzJYvejJBx3saRqdbgEqFx6Tamt88vm6', '75', 'LOCAL', 'https://i.pravatar.cc/150?img=33', NULL, 'ACTIVE', '0945678901', '2800', '10', '90', 't', 'vi', 't', '25', '90', '18000', '2025-11-25'),
    ('c8c83b9d-ef94-43d0-8e47-0d923ca7859a', '2025-11-25 22:44:25.774259+07', 'f', '2025-11-25 23:17:14.079819+07', '1995-08-10', '20', 'user1@viegym.com', 'Trung cấp', 'Lê Văn An', 'Nam', 'GIẢM CÂN', '172', '$2a$10$fAte0TMT/RxmyZ/snGl4qOzJYvejJBx3saRqdbgEqFx6Tamt88vm6', '68', 'LOCAL', 'https://i.pravatar.cc/150?img=12', NULL, 'SUSPENDED', '0923456789', '1800', '8', '60', 't', 'vi', 't', '15', '45', '8500', '2025-11-25');



--
-- TOC entry 5195 (class 0 OID 17000)
-- Dependencies: 231
-- Data for Name: workout_programs; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: workout_programs
INSERT INTO workout_programs (program_id, created_at, deleted, updated_at, duration_weeks, goal, metadata, title, visibility, creator_user_id, description) VALUES
    ('978a3e9c-b5b4-4e6b-850e-c66c2853c728', '2025-12-02 23:25:57.54676+07', 'f', '2025-12-02 23:25:57.54676+07', '2', 'GENERAL_FITNESS', NULL, 'Bài tập đẩy kéo chân Ultimate Push Pull Legs Split của Jeff Nippard (PPL)', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', 'Chương trình Ultimate Push Pull Legs Split (PPL) của Jeff Nippard được thiết kế để tối đa hóa sự phát triển và sức mạnh cơ bắp thông qua các nguyên tắc tập luyện dựa trên bằng chứng. Chương trình 3 ngày mỗi tuần này xen kẽ giữa hai bài tập đẩy, kéo và chân khác nhau mỗi tuần, đảm bảo sự phát triển cân bằng và tăng dần cường độ tập luyện. Chương trình này tận dụng kiến ​​thức và chuyên môn sâu rộng của Jeff Nippard để mang đến một kế hoạch tập luyện hiệu quả và cân bằng cao cho những người tập tạ chuyên nghiệp hướng đến kết quả tối ưu. Chương trình này lý tưởng cho những người tập tạ từ trung cấp đến cao cấp, cam kết tập luyện nghiêm túc.\\nLịch tập luyện đề xuất:\\n- Tuần 1:\\n- Thứ Hai - Đẩy Ngày 1\\n- Thứ Tư - Kéo Ngày 1\\n- Thứ Sáu - Chân Ngày 1\\n- Tuần 2:\\n- Thứ Hai - Đẩy Ngày 2\\n- Thứ Tư - Kéo Ngày 2\\n- Thứ Sáu - Chân Ngày 2\\nLưu ý:\\n- Điều chỉnh tạ theo sức mạnh của bạn và đảm bảo tư thế đúng để đạt được lợi ích tối đa.\\n- Theo dõi tiến trình của bạn và tăng tạ dần dần để tiếp tục thử thách cơ bắp.\\n- Một số bài tập được thiết kế riêng để phù hợp với chương trình của Jeff.'),
    ('6786b3fe-1d0e-4a95-af92-7c5e770a380c', '2025-12-02 23:45:40.726652+07', 'f', '2025-12-02 23:45:40.726652+07', '4', 'GENERAL_FITNESS', NULL, 'Bài tập toàn thân', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', 'Bài tập toàn thân rèn luyện thể chất'),
    ('8e6a5f3d-31e3-4b98-aea2-57caa0647707', '2025-12-02 23:47:44.810146+07', 'f', '2025-12-02 23:47:44.810146+07', '1', 'GENERAL_FITNESS', NULL, '5 ngày lấy lại cơ bắp', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', 'Với 5 thói quen này bạn có thể làm tất cả những gì bạn có thể làm trong một tuần, đó là một trong những nhật ký liên quan đến cấp trên và cấp dưới, bạn có thể tổ chức lại như một điều kỳ lạ.'),
    ('12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', '2025-12-02 23:49:36.539238+07', 'f', '2025-12-02 23:49:36.539238+07', '4', 'GENERAL_FITNESS', NULL, 'PHÒNG TẬP GIAI ĐOẠN 2', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', ''),
    ('d8bcb73c-534d-424a-b6b9-0bfd0a3ee965', '2025-12-02 23:50:13.119088+07', 'f', '2025-12-02 23:50:13.119088+07', '1', 'GENERAL_FITNESS', NULL, 'Chia nhỏ lộ trình 5 ngày từ gầy đến săn chắc', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', 'Chương trình chia nhỏ 5 ngày này được thiết kế dành riêng cho những người tập trung cấp, những người đã bắt đầu với thân hình gầy gò và đã vượt qua những nguyên tắc cơ bản. Bạn đã chứng minh được sự kiên trì của mình, tăng cân vượt trội hơn 10-20% so với số cân ban đầu. Giờ là lúc thúc đẩy quá trình tăng trưởng nhanh chóng.\\nChương trình này phân tách các nhóm cơ chính một cách chiến lược trong suốt tuần—Ngực, Lưng, Chân, Vai và Tay—cho phép tập trung khối lượng lớn, cường độ cao vào từng nhóm cơ. Bằng cách dành trọn buổi tập cho từng nhóm cơ, bạn sẽ tối đa hóa áp lực trao đổi chất và căng cơ học, những yếu tố chính thúc đẩy sự phì đại, đồng thời đảm bảo khả năng phục hồi vượt trội. Đây không chỉ là một bài tập thông thường; mà là một bản thiết kế được tính toán kỹ lưỡng, giúp những người có vóc dáng thon gọn vượt qua giai đoạn trì trệ, tăng cơ bắp và tạo nên vóc dáng ấn tượng, săn chắc. Nền tảng của bạn đã được thiết lập. Giờ thì, hãy bắt đầu hành trình thay đổi của bạn.'),
    ('b01b570f-969d-4107-b0e1-926392c91a54', '2025-12-02 23:50:53.243851+07', 'f', '2025-12-02 23:50:53.243851+07', '2', 'MUSCLE_GAIN', NULL, 'Thể dục dụng cụ/Trọng lượng cơ thể', 'PUBLIC', '37281177-8b54-45ad-bf37-568deb8e33cd', '');



--
-- TOC entry 5196 (class 0 OID 17007)
-- Dependencies: 232
-- Data for Name: workout_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--


-- Data for table: workout_sessions
INSERT INTO workout_sessions (session_id, created_at, deleted, updated_at, duration_minutes, notes, session_date, program_id, user_id) VALUES
    ('3cc67078-551c-4bb8-8802-9a88680044a0', '2025-12-18 01:28:33.962265+07', 'f', '2025-12-18 01:30:16.170699+07', '1', NULL, '2025-12-18 01:28:33.949+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('843cbac4-9e22-4fab-92fc-b05395bce9b4', '2025-12-05 00:42:07.09292+07', 'f', '2025-12-05 00:42:20.989367+07', '1', 'Lower Body', '2025-12-05 00:42:07.059+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('115cc836-ddd6-4ca1-a0fc-e2d9fbda1d1f', '2025-12-18 02:04:04.600665+07', 'f', '2025-12-18 02:16:19.888739+07', '12', NULL, '2025-12-18 02:04:04.521+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('e4420abf-9939-4ee3-909a-cc3b868a509b', '2025-12-18 02:16:28.176075+07', 'f', '2025-12-18 02:20:00.645531+07', '3', NULL, '2025-12-18 02:16:28.144+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('2ad1eec3-53ee-4f69-8b14-4454c0dc957c', '2025-12-05 00:47:52.82729+07', 'f', '2025-12-05 00:47:52.82729+07', '0', 'Lower Body', '2025-12-05 00:47:52.81+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('8f99d9ae-c6c9-4494-a9e8-5ea8b926147d', '2025-12-18 02:25:20.906647+07', 'f', '2025-12-18 02:29:05.7541+07', '3', NULL, '2025-12-18 02:25:20.837+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('8761147b-5725-4524-839d-ac52196e7729', '2025-12-18 02:29:08.186223+07', 'f', '2025-12-18 02:35:21.568826+07', '6', NULL, '2025-12-18 02:29:08.143+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('f7395435-3eb4-44ca-a778-54c51ac1d14c', '2025-12-18 02:35:37.42675+07', 'f', '2025-12-18 02:47:04.954389+07', '11', NULL, '2025-12-18 02:35:37.406+07', '8e6a5f3d-31e3-4b98-aea2-57caa0647707', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('76e50f08-3dd0-4c1c-95af-117a29db5f7d', '2025-12-05 01:04:54.377007+07', 'f', '2025-12-05 01:04:56.242713+07', '1', 'Lower Body', '2025-12-05 01:04:54.269+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('554540d7-414e-4c04-a7be-3637732b0570', '2025-12-05 01:05:08.00115+07', 'f', '2025-12-05 01:05:10.296322+07', '1', 'Full Body', '2025-12-05 01:05:07.985+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('24e66c3e-ddc2-4a3b-a847-8cb9d46aa29e', '2025-12-18 02:51:26.826186+07', 'f', '2025-12-18 02:51:33.668508+07', '1', NULL, '2025-12-18 02:51:26.803+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('d5b6c748-95f2-44c6-b8d0-b3935afe05b5', '2025-12-18 02:51:35.955101+07', 'f', '2025-12-18 02:51:50.619801+07', '1', NULL, '2025-12-18 02:51:35.94+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('09721a3c-5c4e-451f-ad04-bb7e364b4597', '2025-12-18 02:53:46.818094+07', 'f', '2025-12-18 02:58:36.310125+07', '4', NULL, '2025-12-18 02:53:46.806+07', 'd8bcb73c-534d-424a-b6b9-0bfd0a3ee965', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('bf74d9ef-d983-46f3-adbe-2deb6989e8a1', '2025-12-18 02:58:47.147717+07', 'f', '2025-12-18 02:58:51.71882+07', '1', NULL, '2025-12-18 02:58:47.132+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('dbb628c9-c2f7-4e6a-b00e-29df26a8bc06', '2025-12-18 02:59:06.707661+07', 'f', '2025-12-18 02:59:11.072501+07', '1', NULL, '2025-12-18 02:59:06.689+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('31dafe47-05f8-4db3-8cbe-e86cd9c34aaf', '2025-12-18 03:33:36.727519+07', 'f', '2025-12-18 03:33:49.893408+07', '1', 'Bài tập đẩy kéo chân Ultimate Push Pull Legs Split của Jeff Nippard (PPL)', '2025-12-18 03:33:36.713+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('3645cf88-6f3f-4f4e-ad09-2547fa3a2a29', '2025-12-18 03:36:04.53317+07', 'f', '2025-12-18 03:36:04.53317+07', '0', 'PHÒNG TẬP GIAI ĐOẠN 2', '2025-12-18 03:36:04.524+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('cef3debf-6c49-4ee3-b495-b4d2d7b2c6bc', '2025-12-18 03:38:09.066212+07', 'f', '2025-12-18 03:38:09.066212+07', '0', 'PHÒNG TẬP GIAI ĐOẠN 2', '2025-12-18 03:38:09.051+07', '12fc1e11-dc38-4fbc-84f5-df4ef96a8da4', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('7af03a49-ff46-4a5b-ba23-bbf7abe0b7b1', '2025-12-18 03:38:45.587334+07', 'f', '2025-12-18 03:38:45.587334+07', '0', 'Bài tập toàn thân', '2025-12-18 03:38:45.578+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('a52778c0-d319-44c1-888a-40a53daf0e90', '2025-12-18 03:40:25.562598+07', 'f', '2025-12-18 03:40:29.639706+07', '1', 'Bài tập toàn thân', '2025-12-18 03:40:25.543+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', 'e50f699c-fad5-4b88-80cf-613145fa27e6'),
    ('37e7088a-2bad-4ff5-bf62-87a608fedc7d', '2025-12-18 04:19:17.486595+07', 'f', '2025-12-18 04:22:27.823789+07', '3', NULL, '2025-12-18 04:19:17.475+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('c07f5719-1ea2-4053-96a9-54bce4362ec3', '2025-11-29 23:38:37.036069+07', 'f', '2025-11-29 23:39:13.303084+07', '1', 'Full Body', '2025-11-29 23:38:36.965+07', NULL, '299f7701-a20b-49ff-adda-a748525cc7c0'),
    ('ff86c86f-4e84-4a5b-af74-4800cc01852f', '2025-11-29 23:46:31.345045+07', 'f', '2025-11-29 23:47:38.328604+07', '1', 'Lower Body', '2025-11-29 23:46:31.321+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('59e218b9-4417-4a7b-808f-9453edad4229', '2025-11-30 00:20:36.949684+07', 'f', '2025-11-30 00:21:34.538888+07', '1', 'Full Body', '2025-11-30 00:20:36.872+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('98ad19d1-cff1-475e-b74f-6542ed2a1d5c', '2025-11-30 00:21:37.885377+07', 'f', '2025-11-30 00:25:40.839651+07', '4', 'Full Body', '2025-11-30 00:21:37.856+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('795a85ee-0102-496b-a6ed-2fb42dbc1a16', '2025-11-30 00:25:44.718546+07', 'f', '2025-11-30 00:27:13.695622+07', '1', 'Cardio', '2025-11-30 00:25:44.696+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('7d182339-1f3f-43b5-8de8-b2eb29a89c15', '2025-11-30 00:27:28.348266+07', 'f', '2025-11-30 00:35:49.598284+07', '8', 'Full Body', '2025-11-30 00:27:28.332+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('32f1fdfe-e375-46b1-87e8-2ccfa2e95434', '2025-11-30 00:44:32.840945+07', 'f', '2025-11-30 00:47:54.830236+07', '3', 'Lower Body', '2025-11-30 00:44:32.712+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('ae9ac13f-e6f9-467b-af99-4730dab26d83', '2025-11-30 13:14:28.765794+07', 'f', '2025-11-30 13:14:39.898093+07', '1', 'Full Body', '2025-11-30 13:14:28.733+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('f46e00fb-568b-4401-a2a4-b3f2ba826aac', '2025-11-30 13:15:25.596517+07', 'f', '2025-11-30 13:15:37.078096+07', '1', 'Lower Body', '2025-11-30 13:15:25.583+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('be5a62c8-3a92-4eb5-ad67-5555182922f2', '2025-11-30 13:18:47.755085+07', 'f', '2025-11-30 13:18:50.361462+07', '1', 'Full Body', '2025-11-30 13:18:47.74+07', NULL, 'fcf0a003-ee9a-4c1a-8f3e-bd7b5b0b3aee'),
    ('34f90031-f656-4f9d-83d7-34b530d0a10f', '2025-12-01 17:32:03.221301+07', 'f', '2025-12-01 17:32:05.937733+07', '1', 'Lower Body', '2025-12-01 17:32:03.187+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('7473d775-ef47-42f1-91ba-66ad3eae339f', '2025-12-05 01:05:40.604604+07', 'f', '2025-12-05 01:07:30.235214+07', '1', 'Full Body', '2025-12-05 01:05:40.58+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('bc9ee8c8-73d8-47a3-9333-fbc6d809a990', '2025-12-05 01:07:45.684514+07', 'f', '2025-12-05 01:08:02.51815+07', '1', NULL, '2025-12-05 01:07:45.666+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('4f1f4b0d-2241-4f24-8d7c-67b9b9f08ebf', '2025-12-05 01:09:14.318362+07', 'f', '2025-12-05 01:11:32.790083+07', '2', NULL, '2025-12-05 01:09:14.3+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('3f669904-63d2-4432-8768-83ca02462f30', '2025-12-05 01:11:53.101066+07', 'f', '2025-12-05 01:11:55.928781+07', '1', NULL, '2025-12-05 01:11:53.077+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('51bc92e4-7b53-4d61-99f4-af8ba7d2a58a', '2025-12-05 01:12:12.163327+07', 'f', '2025-12-05 01:14:41.580492+07', '2', NULL, '2025-12-05 01:12:12.151+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('fbee7ba2-a219-4361-9554-4475479cb701', '2025-12-05 01:14:45.79201+07', 'f', '2025-12-05 01:14:50.290784+07', '1', NULL, '2025-12-05 01:14:45.777+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('8e4573d0-fb51-4136-8ad2-2b71411abfb0', '2025-12-05 01:15:43.340161+07', 'f', '2025-12-05 01:16:41.647044+07', '1', NULL, '2025-12-05 01:15:43.318+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('f66194be-4d15-4761-bf78-125f1fa8eeb0', '2025-12-05 01:16:45.754555+07', 'f', '2025-12-05 01:16:51.45073+07', '1', NULL, '2025-12-05 01:16:45.736+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('93e86ab8-191f-4377-ac32-edda475895c8', '2025-12-05 01:20:06.828201+07', 'f', '2025-12-05 01:21:27.837901+07', '1', NULL, '2025-12-05 01:20:06.751+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('1ae82999-508c-422b-b399-f667c7500cfd', '2025-12-05 01:21:31.784002+07', 'f', '2025-12-05 01:23:26.593765+07', '1', NULL, '2025-12-05 01:21:31.759+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('09495ad3-86b2-4b8e-83d5-5e0609ae7ee8', '2025-12-05 20:55:01.924979+07', 'f', '2025-12-05 20:55:01.924979+07', '0', 'Lower Body', '2025-12-05 20:55:01.902+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('afbe79aa-c9b1-4dc4-ba50-61e71ac8700c', '2025-12-05 21:00:40.393548+07', 'f', '2025-12-05 21:00:55.282086+07', '1', 'Full Body', '2025-12-05 21:00:40.342+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('014ab8fc-06b1-4e96-ba1d-1673dacd73cc', '2025-12-06 11:33:03.516105+07', 'f', '2025-12-06 11:33:03.516105+07', '0', 'Lower Body', '2025-12-06 11:33:03.486+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('462560d0-2547-4371-8246-08237bbbc291', '2025-12-12 23:07:44.467105+07', 'f', '2025-12-12 23:07:51.587527+07', '1', 'Lower Body', '2025-12-12 23:07:44.433+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('b296f71c-b8ce-4ae3-81c4-406e69a75bc0', '2025-12-12 23:07:57.171678+07', 'f', '2025-12-12 23:08:09.735129+07', '1', 'Lower Body', '2025-12-12 23:07:57.155+07', '978a3e9c-b5b4-4e6b-850e-c66c2853c728', '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('15df5b40-3ba9-4acc-9f5c-4151097b8fb9', '2025-12-13 22:40:13.088203+07', 'f', '2025-12-13 22:40:16.861743+07', '1', 'Lower Body', '2025-12-13 22:40:13.058+07', '8e6a5f3d-31e3-4b98-aea2-57caa0647707', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('beed7f0c-a85b-4eaf-8c2b-b58954bb1772', '2025-12-15 19:46:20.861606+07', 'f', '2025-12-15 19:46:44.032062+07', '1', NULL, '2025-12-15 19:46:20.783+07', NULL, '37281177-8b54-45ad-bf37-568deb8e33cd'),
    ('152c2615-1f55-40f7-b60c-afc4a0d060c8', '2025-12-18 01:11:43.142895+07', 'f', '2025-12-18 01:14:31.300947+07', '2', NULL, '2025-12-18 01:11:43.1+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('3320221f-4915-405b-bbdf-dddefb7a5d88', '2025-12-18 01:25:44.126274+07', 'f', '2025-12-18 01:28:18.29614+07', '2', NULL, '2025-12-18 01:25:44.066+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('5803e8b3-6b20-46b2-90bf-e32ba4ea2976', '2025-12-18 01:28:20.848703+07', 'f', '2025-12-18 01:28:30.293563+07', '1', NULL, '2025-12-18 01:28:20.837+07', NULL, '8a79865b-85ed-4487-8a77-c560bb853ede'),
    ('a1c7c42a-a047-489e-993f-6c9a1766522b', '2025-12-18 04:30:38.838427+07', 'f', '2025-12-18 04:30:41.608722+07', '1', 'Bài tập toàn thân', '2025-12-18 04:30:38.816+07', '6786b3fe-1d0e-4a95-af92-7c5e770a380c', '8a79865b-85ed-4487-8a77-c560bb853ede');



--
-- TOC entry 4962 (class 2606 OID 26388)
-- Name: booking_sessions booking_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_sessions
    ADD CONSTRAINT booking_sessions_pkey PRIMARY KEY (id);


--
-- TOC entry 4965 (class 2606 OID 26396)
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- TOC entry 4981 (class 2606 OID 26531)
-- Name: coach_balances coach_balances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_balances
    ADD CONSTRAINT coach_balances_pkey PRIMARY KEY (id);


--
-- TOC entry 4958 (class 2606 OID 26368)
-- Name: coach_clients coach_clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_clients
    ADD CONSTRAINT coach_clients_pkey PRIMARY KEY (coach_client_id);


--
-- TOC entry 4967 (class 2606 OID 26404)
-- Name: coach_time_slots coach_time_slots_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_time_slots
    ADD CONSTRAINT coach_time_slots_pkey PRIMARY KEY (id);


--
-- TOC entry 4985 (class 2606 OID 26541)
-- Name: coach_transactions coach_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_transactions
    ADD CONSTRAINT coach_transactions_pkey PRIMARY KEY (id);


--
-- TOC entry 4889 (class 2606 OID 16905)
-- Name: community_posts community_posts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.community_posts
    ADD CONSTRAINT community_posts_pkey PRIMARY KEY (post_id);


--
-- TOC entry 4891 (class 2606 OID 16912)
-- Name: exercise_media exercise_media_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercise_media
    ADD CONSTRAINT exercise_media_pkey PRIMARY KEY (media_id);


--
-- TOC entry 4936 (class 2606 OID 17158)
-- Name: exercise_tags exercise_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercise_tags
    ADD CONSTRAINT exercise_tags_pkey PRIMARY KEY (exercise_id, tag_id);


--
-- TOC entry 4893 (class 2606 OID 16919)
-- Name: exercises exercises_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercises
    ADD CONSTRAINT exercises_pkey PRIMARY KEY (exercise_id);


--
-- TOC entry 4897 (class 2606 OID 16924)
-- Name: health_logs health_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.health_logs
    ADD CONSTRAINT health_logs_pkey PRIMARY KEY (health_id);


--
-- TOC entry 4969 (class 2606 OID 26477)
-- Name: notification_preferences notification_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_pkey PRIMARY KEY (id);


--
-- TOC entry 4973 (class 2606 OID 26485)
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- TOC entry 4899 (class 2606 OID 16945)
-- Name: nutrition_logs nutrition_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nutrition_logs
    ADD CONSTRAINT nutrition_logs_pkey PRIMARY KEY (nutrition_id);


--
-- TOC entry 4944 (class 2606 OID 26297)
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (token_id);


--
-- TOC entry 4975 (class 2606 OID 26507)
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- TOC entry 4903 (class 2606 OID 16952)
-- Name: post_comments post_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_comments
    ADD CONSTRAINT post_comments_pkey PRIMARY KEY (comment_id);


--
-- TOC entry 4905 (class 2606 OID 16957)
-- Name: post_likes post_likes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_likes
    ADD CONSTRAINT post_likes_pkey PRIMARY KEY (like_id);


--
-- TOC entry 4909 (class 2606 OID 16964)
-- Name: post_media post_media_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_media
    ADD CONSTRAINT post_media_pkey PRIMARY KEY (post_media_id);


--
-- TOC entry 4956 (class 2606 OID 26350)
-- Name: post_reports post_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_reports
    ADD CONSTRAINT post_reports_pkey PRIMARY KEY (report_id);


--
-- TOC entry 4911 (class 2606 OID 16971)
-- Name: program_exercises program_exercises_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_exercises
    ADD CONSTRAINT program_exercises_pkey PRIMARY KEY (program_exercise_id);


--
-- TOC entry 4942 (class 2606 OID 18059)
-- Name: program_media program_media_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_media
    ADD CONSTRAINT program_media_pkey PRIMARY KEY (id);


--
-- TOC entry 4948 (class 2606 OID 26311)
-- Name: program_ratings program_ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_ratings
    ADD CONSTRAINT program_ratings_pkey PRIMARY KEY (rating_id);


--
-- TOC entry 4913 (class 2606 OID 16976)
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (token_id);


--
-- TOC entry 4917 (class 2606 OID 16982)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (role_id);


--
-- TOC entry 4952 (class 2606 OID 26316)
-- Name: saved_programs saved_programs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_programs
    ADD CONSTRAINT saved_programs_pkey PRIMARY KEY (saved_id);


--
-- TOC entry 4921 (class 2606 OID 16987)
-- Name: session_exercise_logs session_exercise_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.session_exercise_logs
    ADD CONSTRAINT session_exercise_logs_pkey PRIMARY KEY (log_id);


--
-- TOC entry 4938 (class 2606 OID 17163)
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (tag_id);


--
-- TOC entry 4915 (class 2606 OID 17018)
-- Name: refresh_tokens uk1yihy5j142kjit22kgccjixro; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk1yihy5j142kjit22kgccjixro UNIQUE (refresh_token);


--
-- TOC entry 4907 (class 2606 OID 17016)
-- Name: post_likes uk5l2rj28vw5oj6f7ox746grokg; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_likes
    ADD CONSTRAINT uk5l2rj28vw5oj6f7ox746grokg UNIQUE (post_id, user_id);


--
-- TOC entry 4928 (class 2606 OID 17025)
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- TOC entry 4954 (class 2606 OID 26320)
-- Name: saved_programs uk6lncjgmppgrn4yvm0r4vv6n0q; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_programs
    ADD CONSTRAINT uk6lncjgmppgrn4yvm0r4vv6n0q UNIQUE (program_id, user_id);


--
-- TOC entry 4923 (class 2606 OID 17022)
-- Name: user_roles uka9dydk3dj4qb8cvmjijqnrg5t; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT uka9dydk3dj4qb8cvmjijqnrg5t UNIQUE (user_id, role_id);


--
-- TOC entry 4950 (class 2606 OID 26318)
-- Name: program_ratings ukab4xqdlarvx211e2pf0ag7i09; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_ratings
    ADD CONSTRAINT ukab4xqdlarvx211e2pf0ag7i09 UNIQUE (program_id, user_id);


--
-- TOC entry 4960 (class 2606 OID 26370)
-- Name: coach_clients ukb02iq2ihkbrcpma2er4pnqij; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_clients
    ADD CONSTRAINT ukb02iq2ihkbrcpma2er4pnqij UNIQUE (coach_id, client_id);


--
-- TOC entry 4901 (class 2606 OID 17014)
-- Name: nutrition_logs ukgoir9w3xud6orgx4gy69eykh3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nutrition_logs
    ADD CONSTRAINT ukgoir9w3xud6orgx4gy69eykh3 UNIQUE (user_id, log_date, meal_type);


--
-- TOC entry 4983 (class 2606 OID 26543)
-- Name: coach_balances ukheq2wy6iylnpap3vjhgdnahi2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_balances
    ADD CONSTRAINT ukheq2wy6iylnpap3vjhgdnahi2 UNIQUE (coach_id);


--
-- TOC entry 4977 (class 2606 OID 26509)
-- Name: payments uklryndveuwa4k5qthti0pkmtlx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uklryndveuwa4k5qthti0pkmtlx UNIQUE (transaction_id);


--
-- TOC entry 4971 (class 2606 OID 26487)
-- Name: notification_preferences ukn2jopkbm16qv3xelbvoyjkd0g; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT ukn2jopkbm16qv3xelbvoyjkd0g UNIQUE (user_id);


--
-- TOC entry 4946 (class 2606 OID 26299)
-- Name: password_reset_tokens uknaejnsff1ladin3sjar0l8xs8; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uknaejnsff1ladin3sjar0l8xs8 UNIQUE (reset_token);


--
-- TOC entry 4919 (class 2606 OID 17020)
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- TOC entry 4979 (class 2606 OID 26511)
-- Name: payments ukropy6yayxm9icqw523wi2xrsp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT ukropy6yayxm9icqw523wi2xrsp UNIQUE (booking_session_id);


--
-- TOC entry 4940 (class 2606 OID 17165)
-- Name: tags ukt48xdq560gs3gap9g7jg36kgc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT ukt48xdq560gs3gap9g7jg36kgc UNIQUE (name);


--
-- TOC entry 4925 (class 2606 OID 16992)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_role_id);


--
-- TOC entry 4930 (class 2606 OID 16999)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4932 (class 2606 OID 17006)
-- Name: workout_programs workout_programs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workout_programs
    ADD CONSTRAINT workout_programs_pkey PRIMARY KEY (program_id);


--
-- TOC entry 4934 (class 2606 OID 17011)
-- Name: workout_sessions workout_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workout_sessions
    ADD CONSTRAINT workout_sessions_pkey PRIMARY KEY (session_id);


--
-- TOC entry 4963 (class 1259 OID 26580)
-- Name: idx_booking_status_expired_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_booking_status_expired_at ON public.booking_sessions USING btree (status, expired_at) WHERE (((status)::text = 'PENDING'::text) AND (deleted = false));


--
-- TOC entry 4894 (class 1259 OID 17012)
-- Name: idx_exercises_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_exercises_name ON public.exercises USING btree (name);


--
-- TOC entry 4895 (class 1259 OID 26292)
-- Name: idx_exercises_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_exercises_type ON public.exercises USING btree (exercise_type);


--
-- TOC entry 4926 (class 1259 OID 17023)
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- TOC entry 4999 (class 2606 OID 17108)
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4987 (class 2606 OID 17033)
-- Name: exercise_media fk1m2gpp0n0uq8m2bk122jjiugc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercise_media
    ADD CONSTRAINT fk1m2gpp0n0uq8m2bk122jjiugc FOREIGN KEY (exercise_id) REFERENCES public.exercises(exercise_id);


--
-- TOC entry 4991 (class 2606 OID 17068)
-- Name: post_comments fk21q7y8a124im4g0l4aaxn4ol1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_comments
    ADD CONSTRAINT fk21q7y8a124im4g0l4aaxn4ol1 FOREIGN KEY (parent_comment_id) REFERENCES public.post_comments(comment_id);


--
-- TOC entry 4986 (class 2606 OID 17028)
-- Name: community_posts fk44o0kkmkldhul00k2lm08bqje; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.community_posts
    ADD CONSTRAINT fk44o0kkmkldhul00k2lm08bqje FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5018 (class 2606 OID 26376)
-- Name: coach_clients fk4xl5mi0hk8kqhwbwcj9j9mo2a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_clients
    ADD CONSTRAINT fk4xl5mi0hk8kqhwbwcj9j9mo2a FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 5010 (class 2606 OID 18060)
-- Name: program_media fk5xmsflhggdwhx6iqqvfs4do68; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_media
    ADD CONSTRAINT fk5xmsflhggdwhx6iqqvfs4do68 FOREIGN KEY (program_id) REFERENCES public.workout_programs(program_id);


--
-- TOC entry 5005 (class 2606 OID 17138)
-- Name: workout_programs fk64olri534c9ud4ashgt1yuwrc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workout_programs
    ADD CONSTRAINT fk64olri534c9ud4ashgt1yuwrc FOREIGN KEY (creator_user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5008 (class 2606 OID 17166)
-- Name: exercise_tags fk6oqja90cn3iiuh45qbdglu2av; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercise_tags
    ADD CONSTRAINT fk6oqja90cn3iiuh45qbdglu2av FOREIGN KEY (tag_id) REFERENCES public.tags(tag_id);


--
-- TOC entry 5033 (class 2606 OID 26554)
-- Name: coach_transactions fk8gkt5qqguxjgso834dr04d8qm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_transactions
    ADD CONSTRAINT fk8gkt5qqguxjgso834dr04d8qm FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 5000 (class 2606 OID 17118)
-- Name: session_exercise_logs fk929mqtvf6ry0n4brwu3evqrgt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.session_exercise_logs
    ADD CONSTRAINT fk929mqtvf6ry0n4brwu3evqrgt FOREIGN KEY (session_id) REFERENCES public.workout_sessions(session_id);


--
-- TOC entry 5014 (class 2606 OID 26336)
-- Name: saved_programs fk9c727qo24vkguumggmfv0mcv0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_programs
    ADD CONSTRAINT fk9c727qo24vkguumggmfv0mcv0 FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5027 (class 2606 OID 26493)
-- Name: notifications fk9y21adhxn0ayjhfocscqox7bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5023 (class 2606 OID 26422)
-- Name: chat_messages fkand7mh9iu4kt3n1tn2w9i9of0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fkand7mh9iu4kt3n1tn2w9i9of0 FOREIGN KEY (receiver_id) REFERENCES public.users(user_id);


--
-- TOC entry 5009 (class 2606 OID 17171)
-- Name: exercise_tags fkayfpwtd1j97450vj2u9e0k01i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercise_tags
    ADD CONSTRAINT fkayfpwtd1j97450vj2u9e0k01i FOREIGN KEY (exercise_id) REFERENCES public.exercises(exercise_id);


--
-- TOC entry 4996 (class 2606 OID 17093)
-- Name: post_media fkb47hahpguc2u0gk1y9gojefll; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_media
    ADD CONSTRAINT fkb47hahpguc2u0gk1y9gojefll FOREIGN KEY (post_id) REFERENCES public.community_posts(post_id);


--
-- TOC entry 5032 (class 2606 OID 26544)
-- Name: coach_balances fkb68oi0vnsrdxfsy97fdi3s0nh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_balances
    ADD CONSTRAINT fkb68oi0vnsrdxfsy97fdi3s0nh FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 5028 (class 2606 OID 26568)
-- Name: payments fkb7kq52yfgwdui0i641beqi2dp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkb7kq52yfgwdui0i641beqi2dp FOREIGN KEY (client_id) REFERENCES public.users(user_id);


--
-- TOC entry 5001 (class 2606 OID 17113)
-- Name: session_exercise_logs fkbc6cegh3ieay024pw08n4slj1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.session_exercise_logs
    ADD CONSTRAINT fkbc6cegh3ieay024pw08n4slj1 FOREIGN KEY (exercise_id) REFERENCES public.exercises(exercise_id);


--
-- TOC entry 5034 (class 2606 OID 26549)
-- Name: coach_transactions fkbjjxat7e938duitxjn4ya34ix; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_transactions
    ADD CONSTRAINT fkbjjxat7e938duitxjn4ya34ix FOREIGN KEY (booking_session_id) REFERENCES public.booking_sessions(id);


--
-- TOC entry 5020 (class 2606 OID 26412)
-- Name: booking_sessions fkdcifbb3780fy5yu7ahlx2h9sq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_sessions
    ADD CONSTRAINT fkdcifbb3780fy5yu7ahlx2h9sq FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 5021 (class 2606 OID 26407)
-- Name: booking_sessions fkdqrl7jrn8yem7lgtdacjo8snh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_sessions
    ADD CONSTRAINT fkdqrl7jrn8yem7lgtdacjo8snh FOREIGN KEY (client_id) REFERENCES public.users(user_id);


--
-- TOC entry 4990 (class 2606 OID 17063)
-- Name: nutrition_logs fkehm4gugsnowlb83vsq2kdy6qy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.nutrition_logs
    ADD CONSTRAINT fkehm4gugsnowlb83vsq2kdy6qy FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5019 (class 2606 OID 26371)
-- Name: coach_clients fkeog4ycf0ency2tr7u626jc7yo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_clients
    ADD CONSTRAINT fkeog4ycf0ency2tr7u626jc7yo FOREIGN KEY (client_id) REFERENCES public.users(user_id);


--
-- TOC entry 5006 (class 2606 OID 17148)
-- Name: workout_sessions fkfwqciawyjntpphp080wpa37ge; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workout_sessions
    ADD CONSTRAINT fkfwqciawyjntpphp080wpa37ge FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5024 (class 2606 OID 26427)
-- Name: chat_messages fkgiqeap8ays4lf684x7m0r2729; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fkgiqeap8ays4lf684x7m0r2729 FOREIGN KEY (sender_id) REFERENCES public.users(user_id);


--
-- TOC entry 5002 (class 2606 OID 17128)
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(role_id);


--
-- TOC entry 5003 (class 2606 OID 17133)
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4994 (class 2606 OID 17083)
-- Name: post_likes fkhl7eauuj5a85e8jumxyc0l8rh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_likes
    ADD CONSTRAINT fkhl7eauuj5a85e8jumxyc0l8rh FOREIGN KEY (post_id) REFERENCES public.community_posts(post_id);


--
-- TOC entry 5016 (class 2606 OID 26351)
-- Name: post_reports fkijuu6ubsoeoewoeet0buivv78; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_reports
    ADD CONSTRAINT fkijuu6ubsoeoewoeet0buivv78 FOREIGN KEY (post_id) REFERENCES public.community_posts(post_id);


--
-- TOC entry 5022 (class 2606 OID 26417)
-- Name: booking_sessions fkj85n2jrgsst8xuu71y7nic1nj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.booking_sessions
    ADD CONSTRAINT fkj85n2jrgsst8xuu71y7nic1nj FOREIGN KEY (time_slot_id) REFERENCES public.coach_time_slots(id);


--
-- TOC entry 5029 (class 2606 OID 26517)
-- Name: payments fkj94hgy9v5fw1munb90tar2eje; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkj94hgy9v5fw1munb90tar2eje FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5030 (class 2606 OID 26512)
-- Name: payments fkjfq5job9evp1cplrfempy9t47; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkjfq5job9evp1cplrfempy9t47 FOREIGN KEY (booking_session_id) REFERENCES public.booking_sessions(id);


--
-- TOC entry 5011 (class 2606 OID 26300)
-- Name: password_reset_tokens fkk3ndxg5xp6v7wd4gjyusp15gq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fkk3ndxg5xp6v7wd4gjyusp15gq FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5012 (class 2606 OID 26321)
-- Name: program_ratings fkk48ouu5q53dnwij9d9i3bdmbl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_ratings
    ADD CONSTRAINT fkk48ouu5q53dnwij9d9i3bdmbl FOREIGN KEY (program_id) REFERENCES public.workout_programs(program_id);


--
-- TOC entry 4995 (class 2606 OID 17088)
-- Name: post_likes fkkgau5n0nlewg6o9lr4yibqgxj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_likes
    ADD CONSTRAINT fkkgau5n0nlewg6o9lr4yibqgxj FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5035 (class 2606 OID 26559)
-- Name: coach_transactions fkkybxhmde4qgebhefe8ug13okr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_transactions
    ADD CONSTRAINT fkkybxhmde4qgebhefe8ug13okr FOREIGN KEY (payment_id) REFERENCES public.payments(id);


--
-- TOC entry 4992 (class 2606 OID 17073)
-- Name: post_comments fkl0vhkouibqhyp8fu0i0979wq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_comments
    ADD CONSTRAINT fkl0vhkouibqhyp8fu0i0979wq FOREIGN KEY (post_id) REFERENCES public.community_posts(post_id);


--
-- TOC entry 5025 (class 2606 OID 26432)
-- Name: coach_time_slots fkl82rf2x9gpd8279ha8sxx1u0m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coach_time_slots
    ADD CONSTRAINT fkl82rf2x9gpd8279ha8sxx1u0m FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 5004 (class 2606 OID 17123)
-- Name: user_roles fkljgw07fam7v71ok817u4rvyro; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkljgw07fam7v71ok817u4rvyro FOREIGN KEY (assigned_by) REFERENCES public.users(user_id);


--
-- TOC entry 4997 (class 2606 OID 17103)
-- Name: program_exercises fkocp1s639b8iyw9pbl5okyrtv4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_exercises
    ADD CONSTRAINT fkocp1s639b8iyw9pbl5okyrtv4 FOREIGN KEY (program_id) REFERENCES public.workout_programs(program_id);


--
-- TOC entry 4988 (class 2606 OID 17038)
-- Name: exercises fkp2kcs08pa87cbt6duijofnivm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercises
    ADD CONSTRAINT fkp2kcs08pa87cbt6duijofnivm FOREIGN KEY (created_by) REFERENCES public.users(user_id);


--
-- TOC entry 5017 (class 2606 OID 26356)
-- Name: post_reports fkqi5fmh45u32i63971en0rmrvo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_reports
    ADD CONSTRAINT fkqi5fmh45u32i63971en0rmrvo FOREIGN KEY (reporter_id) REFERENCES public.users(user_id);


--
-- TOC entry 5015 (class 2606 OID 26331)
-- Name: saved_programs fkqv178tu90b67y1j4v4ll5s1oq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_programs
    ADD CONSTRAINT fkqv178tu90b67y1j4v4ll5s1oq FOREIGN KEY (program_id) REFERENCES public.workout_programs(program_id);


--
-- TOC entry 5031 (class 2606 OID 26573)
-- Name: payments fkr9w4k21y4ev59j54yyr6u82x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkr9w4k21y4ev59j54yyr6u82x FOREIGN KEY (coach_id) REFERENCES public.users(user_id);


--
-- TOC entry 4989 (class 2606 OID 17043)
-- Name: health_logs fkrl52rix9oqkb736haaqhtl73f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.health_logs
    ADD CONSTRAINT fkrl52rix9oqkb736haaqhtl73f FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5007 (class 2606 OID 17143)
-- Name: workout_sessions fksc6ierjtv3h4gemww4wrdom8w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.workout_sessions
    ADD CONSTRAINT fksc6ierjtv3h4gemww4wrdom8w FOREIGN KEY (program_id) REFERENCES public.workout_programs(program_id);


--
-- TOC entry 4993 (class 2606 OID 17078)
-- Name: post_comments fksnxoecngu89u3fh4wdrgf0f2g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.post_comments
    ADD CONSTRAINT fksnxoecngu89u3fh4wdrgf0f2g FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5013 (class 2606 OID 26326)
-- Name: program_ratings fkstxbqhgwbg9lqo6onpbevqmij; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_ratings
    ADD CONSTRAINT fkstxbqhgwbg9lqo6onpbevqmij FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 5026 (class 2606 OID 26488)
-- Name: notification_preferences fkt9qjvmcl36i14utm5uptyqg84; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT fkt9qjvmcl36i14utm5uptyqg84 FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4998 (class 2606 OID 17098)
-- Name: program_exercises fkvn4ww1mdqd3ba94a5fscq7m0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.program_exercises
    ADD CONSTRAINT fkvn4ww1mdqd3ba94a5fscq7m0 FOREIGN KEY (exercise_id) REFERENCES public.exercises(exercise_id);


-- Completed on 2025-12-18 21:46:32

--
-- PostgreSQL database dump complete
--

