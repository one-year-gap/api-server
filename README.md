# Holiverse
> 고객 데이터 기반 고객 분석 서비스 (팀: 한살차이)
---

<a id="toc"></a>
## 📚 목차
- [📌 프로젝트 소개](#overview)
- [🛠️ Tech Stack](#tech-stack)
- [🖥️ User UI (고객 화면)](#user-ui)
- [🖥️ Admin UI (관리자 화면)](#admin-ui)
- [📌 핵심 기능](#core-features)
  - [1) 요금제/상품 추천](#feature-1)
  - [2) 알림·쿠폰 발송](#feature-2)
  - [3) 분석 보고서 생성(AI)](#feature-3)
- [⚖️ 기술 선택 근거](#tech-rationale)
  - [Persistence 전략: JPA vs jOOQ](#persistence-strategy)


---

<a id="overview"></a>
## 📌 프로젝트 소개 (Overview)
Holiverse는 **통신사의 고객 데이터를 기반**으로 고객의 특성을 이해하고 분류하는 과정을 서비스로 구현하는 **통합 프로젝트**입니다.  
고객의 **기본 정보**와 **상담 이력** 데이터를 조합하여 고객 성향과 특징을 도출하고, 이를 **서비스 화면(고객/관리자)** 으로 제공합니다.

또한 **CRM/CDP** 관점에서 고객 데이터를 분석해 **추천, 알림 등 행동(Action) 서비스**로 연결함으로써,  
단발성 분석이 아니라 **고객과의 지속적인 연결(Engagement)** 을 지향합니다.


----
<a id="tech-stack"></a>
## 🛠️ Tech Stack

### Frontend
- Next.js (SSR/CSR): 초기 로딩/SEO와 인터랙션을 함께 고려한 렌더링 구조
- TypeScript: 정적 타입 기반 안정적인 UI/데이터 모델링
- Tailwind CSS: 빠른 UI 제작 및 일관된 디자인 시스템
- TanStack Query: 서버 상태 캐싱/동기화, API 호출 표준화
- Zustand: 전역 상태 관리(인증 상태, UI state 등)

### Backend
- Java 17 / Spring Boot 3.x: 도메인 중심 REST API 구축
- Spring Security + JWT(OAuth2 확장): 인증/인가 및 소셜 로그인 연동
- JPA(Hibernate) + QueryDSL: 핵심 도메인 CRUD + 복잡한 조회/통계 쿼리
- Spring Batch: 일 단위 고객 세그먼트/캐릭터 산출, 타겟 리스트 생성

### Data / Storage
- PostgreSQL: 핵심 트랜잭션 데이터(회원/상품/상담/쿠폰/발송이력) 저장

### Infra / DevOps
- Docker Compose: 로컬 개발 환경 표준화(Postgres 등)
- GitHub Actions: CI(빌드/테스트/린트/커버리지) 자동화



------
<a id="user-ui"></a>
## 🖥️ User UI (고객 화면)







<a id="admin-ui"></a>
## 🖥️ Admin UI (관리자 화면)





------
<a id="core-features"></a>
## 📌 핵심 기능 (Overview)

<a id="feature-1"></a>
### 1) 요금제/상품 추천 (고객 캐릭터 기반)

고객이 보유한 데이터(요금제 종류, 데이터 사용량, 웨어러블 기기 사용 여부, 부가서비스 가입 내역 등)를 분석하여  
팀이 정의한 **6가지 고객 캐릭터 중 1개**를 부여합니다.  
부여된 캐릭터를 기준으로 고객에게 **가장 적합한 요금제 및 관련 상품**을 추천합니다.

**입력 데이터 예시**
- 요금제 종류(현재 가입 요금제)
- 데이터 사용량(최근 N일/월 사용량)
- 웨어러블 기기 사용 유무
- 부가서비스 가입/이용 내역
- 그외 여러가지 데이터

**처리 방식**
- 고객 특성(feature)을 생성하고, 규칙/점수 기반으로 6개 캐릭터 중 1개를 매칭
- 캐릭터별 추천 규칙(또는 추천 후보군)을 정의하여 요금제/상품을 추천

**화면 제공(UI)**

<img width="276" height="514" alt="image" src="https://github.com/user-attachments/assets/f755b7a9-63c7-4987-bb1b-9c019da2bd94" />
<img width="280" height="516" alt="image" src="https://github.com/user-attachments/assets/4b1c33fa-73ea-42b8-9288-33648e1a0c91" />
<img width="279" height="517" alt="image" src="https://github.com/user-attachments/assets/af489777-b456-4e4f-bd54-b837efa8478b" />


- 고객에게 부여된 캐릭터를 캐릭터 카드/이미지로 시각화
- 캐릭터에 맞춘 추천 요금제/상품 목록 제공

**운영 방식(배치)**
- 캐릭터 부여는 **하루 1회 배치**로 실행하며, 고객의 최신 데이터를 반영해 일 단위로 갱신
---

<a id="feature-2"></a>
### 2) 고객 유지/이탈 방지를 위한 알림·쿠폰 발송 (세그먼트 기반)

서비스 이용 고객의 **유지(Loyalty)** 와 **이탈 방지(Churn Prevention)** 를 위해,  
고객 데이터를 기반으로 알림/쿠폰 발송 대상을 선정하고 맞춤 혜택을 제공합니다.

> 정량 기준은 운영 정책에 따라 변경 가능한 **Rule Config** 형태로 설계합니다.

---
<a id="feature-3"></a>
#### 타겟 세그먼트 & 정량 기준(예시)

**A. 충성 고객(Loyalty)**
- 조건 예시
  - 가입 기간 **≥ 10년**
  - 멤버십 **VIP 이상** 유지(예: VIP/VVIP)
  - 최근 **90일 상담 0건** 또는 상담 만족도/긍정 비율 **≥ 80%**
- 혜택 예시
  - 데이터 쿠폰(예: **5GB**)
  - 요금제 **1개월 할인 쿠폰(예: 10%)**

**B. 이탈 위험 고객(Churn Risk)**
- 조건 예시(아래 중 2개 이상 충족 시 “이탈 위험”)
  - 약정 만료 **D-30 이내**
  - 최근 30일 상담에서 **불만/부정 키워드 ≥ 3회**
  - **욕설 키워드 1회 이상** 탐지(즉시 위험 플래그)
  - 데이터 사용량 급감: 최근 7일 평균이 직전 4주 평균 대비 **30% 이상 감소**
- 혜택 예시
  - 약정 갱신 유도 쿠폰(할인/추가 데이터/부가서비스 체험 등)

**C. 이벤트/기타 캠페인**
- 생일 캠페인
  - 조건: **생일 당일**
  - 혜택: 서비스 내 상품/부가서비스 **할인 쿠폰**
- 사용량 리워드 캠페인
  - 조건: 월간 데이터 사용량 **상위 1%** 또는 **Top N명**
  - 혜택: 데이터 쿠폰(예: **10GB**)
 
#### 화면 제공(UI)

<img width="408" height="808" alt="image" src="https://github.com/user-attachments/assets/88fd281e-16cc-4619-8e65-e921fa35ccae" />

- 고객 앱/웹에서 알림 수신 및 쿠폰 확인 화면 제공

#### 알림 발송 플로우(다이어그램)


<img width="7985" height="410" alt="Untitled diagram-2026-02-19-074550" src="https://github.com/user-attachments/assets/6323f88e-c309-4819-b5ca-2d107bf4cd77" />

---

### 3) 분석 보고서 생성 (AI 기반 인사이트 리포트)

관리자 페이지에서 수집·집계된 지표(예: 고객 세그먼트 분포, 이탈 위험 추이, 상담 이슈 키워드, 요금제/상품 이용 패턴 등)를 기반으로  
AI를 활용해 **향후 비즈니스 방향성**을 제안하고, 실행 가능한 인사이트를 도출하는 기능입니다.

**목표**
- “데이터 조회”를 넘어, 관리자가 바로 의사결정을 내릴 수 있도록
  - 현재 상태 요약 → 문제/원인 가설 → 개선 포인트 → 실행 항목(Action Items)까지 자동 생성

---

#### 입력 데이터(예시)
- 세그먼트 지표: 고객 유형 분포, 전환/유지율, 세그먼트별 특징 요약
- 이탈/리텐션 지표: 약정 만료 임박 고객 수, 이탈 위험 고객 추이, 쿠폰 캠페인 반응(발송/사용/전환)
- 상담 데이터: 상담 카테고리 TOP, 불만/부정 키워드 빈도, 최근 증가 이슈
- 이용 패턴: 데이터 사용량 분포(상위/급감), 부가서비스 가입률, 요금제 변경 패턴

---

#### 산출물(관리자 화면)

<img width="924" height="649" alt="image" src="https://github.com/user-attachments/assets/33226f49-8e44-41ec-a87f-1cc60a2e2317" />

- 보고서 생성전 UI
<img width="1002" height="701" alt="image" src="https://github.com/user-attachments/assets/29411486-0784-4213-89ed-a266272470a5" />

- 보고서 생성후 UI



- **AI 분석 리포트 카드/문서**
  - 핵심 요약(Executive Summary)
  - 주요 인사이트 TOP N (근거 지표 포함)
  - 리스크/기회 요인
  - 권장 액션(우선순위/대상 세그먼트/예상 효과)
- (미정) 리포트 다운로드(PDF) / 공유 링크 / 저장된 리포트 히스토리

---

#### AI 적용 방식(설계 포인트)
- 관리자 지표를 그대로 넣지 않고, AI가 이해하기 쉬운 형태로 **정규화/요약된 컨텍스트**로 구성합니다.
  - 예: 표/JSON 형태
- 보고서 품질을 위해 “데이터 컨텍스트 + 리포트 템플릿 프롬프트”를 분리합니다.
  - 컨텍스트: 수치/분포/추이/키워드(근거 데이터)
  - 프롬프트: 보고서 구조(요약→인사이트→액션)와 출력 형식(마크다운/섹션 고정)

> 기획 단계에서 “어떤 지표를 어떤 포맷으로 정리해 AI에 제공할지”를 우선 정의하고,
---
#### 리포트 생성 플로우(예시)
<img width="5412" height="410" alt="Untitled diagram-2026-02-19-075111" src="https://github.com/user-attachments/assets/4298fc1e-1e63-4a59-ba59-eaca2af480de" />

----------------
<a id="tech-rationale"></a>
## ⚖️ 기술 선택 근거
----------------

<a id="persistence-strategy"></a>
## Persistence 전략: Customer(Core)는 JPA, Admin/Analytics는 jOOQ

Holiverse는 **업무 성격이 다른 두 영역**을 함께 다룹니다.

- **Customer(Core) 영역**: 회원/상담/상품 등 트랜잭션 중심 CRUD(OLTP)
- **Admin/Analytics/Reco 영역**: 집계·분석·랭킹 계산 등 SQL 중심 대량 처리(OLAP/Batch)

따라서 우리는 **Core(Customer)에는 JPA**, **Admin/Analytics에는 jOOQ**를 사용해 각각의 강점을 극대화합니다.

---


### 1) JPA (Customer/Core에 사용)

#### 장점
- **생산성이 높다**: 엔티티 기반으로 CRUD를 빠르게 구현 가능
- 트랜잭션 범위에서 dirty checking이 동작하여 **도메인 로직 구현이 편하다**
- 연관관계 매핑을 통해 **비즈니스 규칙을 코드로 표현**하기 좋다(특히 core 도메인)

#### 단점
- **대량 처리/집계/벌크 업데이트에 취약**
  - 대량 INSERT/UPDATE는 영속성 컨텍스트 관리(flush/clear), batch size 튜닝 등 “JPA식 최적화”가 필요
  - 벌크 update는 영속성 컨텍스트와 DB 불일치가 발생해 **운영 사고 포인트**가 될 수 있음
- CTE/윈도우 함수/복잡 조인처럼 **분석형 SQL을 끌고 가기 어렵고**, 결국 Native Query 비중이 증가
- N+1, 지연 로딩 등으로 **실제 실행 쿼리가 코드만으로는 예측이 어려운 경우**가 있음

#### 적용 범위(우리 기준)
- `core` 스키마: 로그인/인증, 고객 CRUD, 상담 기록, 상품/요금제 카탈로그 등 **OLTP 트랜잭션성 CRUD**
- 관리자 영역도 “CRUD + 단순 통계” 수준이면 JPA로 충분

---

### 2) jOOQ (Admin/Analytics/Reco에 사용)

#### 장점
- **SQL을 그대로 작성**하면서도 타입 세이프(코드 생성) + IDE 지원이 강함
- 실행 쿼리가 명확해 **성능 예측/디버깅/튜닝이 용이**
- 분석/집계에서 자주 쓰는 패턴(CTE, 윈도우 함수, upsert, 복잡 조인)을 자연스럽게 표현 가능
- 스키마/권한 기반으로 경계를 강제하는 구조에서 **DB 중심 read-model/집계 모델**을 만들기 적합

#### 단점
- 엔티티 기반 도메인 모델링(JPA 스타일)에는 부적합 (SQL 중심 접근)
- 코드 생성/스키마 변경 관리(Flyway 연계) 등 **초기 셋업 비용**이 존재
- 팀의 SQL 숙련도에 따라 **러닝커브**가 생길 수 있음

#### 적용 범위(우리 기준)
- `analytics` 스키마: 피처 테이블 생성, 페르소나/리스크 집계, KPI 산출
- `reco` 스키마: 추천 로그 기반 집계, 랭킹 계산을 위한 조합 조회
- 배치/집계 중심 워크로드 전반

---

## 결론: “도메인 중심(OLTP) vs 데이터 계산(OLAP)”을 분리한다

### 1) 성능/운영 관점
추천/분석은 “데이터를 계산”하는 일이 많고, 병목은 결국 SQL에서 발생합니다.  
따라서 **쿼리가 명확하고 튜닝 포인트가 분명해야** 운영이 가능합니다.

- JPA는 쿼리가 간접 생성되어 **병목 원인 추적이 느려질 수 있음**
- 배치에서 대량 처리를 JPA로 하면 flush/clear, fetch 전략, batch size 등 **ORM 최적화 러닝커브**가 커짐
- 분석/집계는 처음부터 **SQL 기반(jOOQ/JDBC)** 으로 가는 편이 합리적

### 2) 팀 복잡도/일관성 관점
“JPA만”은 초반 생산성은 높지만, 집계/추천이 늘수록 Native Query가 섞이며  
결국 **JPA + Native 혼합으로 복잡도가 상승**할 가능성이 큽니다.  
그래서 분석/배치 영역은 **처음부터 jOOQ로 통일**해 복잡도를 관리합니다.













<br/>
<!-- CI 빌드 상태 -->
<a href="https://github.com/one-year-gap/api-server/actions/workflows/test-coverage.yml">
  <img src="https://github.com/one-year-gap/api-server/actions/workflows/test-coverage.yml/badge.svg?branch=main" alt="CI Build" height="28"/>
</a>
&nbsp;
<!-- JaCoCo 커버리지 -->
<a href="https://github.com/one-year-gap/api-server/actions/workflows/test-coverage.yml">
  <img src="https://img.shields.io/badge/coverage-JaCoCo-brightgreen?style=flat-square&logo=java&logoColor=white&labelColor=2d2d2d" alt="JaCoCo Coverage" height="28"/>
</a>
&nbsp;
<!-- ArchUnit -->
<a href="https://github.com/one-year-gap/api-server/actions/workflows/test-coverage.yml">
  <img src="https://img.shields.io/badge/arch-ArchUnit-blueviolet?style=flat-square&logo=checkmarx&logoColor=white&labelColor=2d2d2d" alt="ArchUnit" height="28"/>
</a>
<br/><br/>
<!-- 스택 배지 -->
<img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white&labelColor=2d2d2d" alt="Java 17" height="24"/>
&nbsp;
<img src="https://img.shields.io/badge/Spring_Boot-3.5.2-6DB33F?style=flat-square&logo=springboot&logoColor=white&labelColor=2d2d2d" alt="Spring Boot" height="24"/>
&nbsp;
<img src="https://img.shields.io/badge/Gradle-8.11.1-02303A?style=flat-square&logo=gradle&logoColor=white&labelColor=2d2d2d" alt="Gradle" height="24"/>
&nbsp;
<img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white&labelColor=2d2d2d" alt="PostgreSQL" height="24"/>
<br/><br/>
<!-- 구분선 -->
<img src="https://capsule-render.vercel.app/api?type=rect&color=gradient&customColorList=12,16,20&height=3&section=header" width="100%"/>
