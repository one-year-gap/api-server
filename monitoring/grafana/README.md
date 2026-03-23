# Grafana Monitoring

이 디렉터리는 앱에서 노출하는 Prometheus 메트릭과 Grafana 대시보드 초안을 담고 있습니다.

## 포함된 대시보드

- `holliverse-customer-observability-dashboard.json`
  - 추천 API end-to-end 시간
  - 추천 대기 시간
  - 추천 pending future 수
  - FastAPI trigger 결과
  - user-log publish 성공/실패
  - user-log 배치 크기
  - admin internal log-feature 호출 지연
  - 비동기 executor queue / active thread

## 새로 추가된 메트릭

- `holliverse.recommendation.requests{outcome=*}`
- `holliverse.recommendation.duration{outcome=*,source=*}`
- `holliverse.recommendation.wait.duration{outcome=*}`
- `holliverse.recommendation.fastapi.trigger{status=*}`
- `holliverse.recommendation.kafka.consume.duration{outcome=*}`
- `holliverse.recommendation.pending.size`
- `holliverse.executor.pool.size{executor=*}`
- `holliverse.executor.active.count{executor=*}`
- `holliverse.executor.queue.size{executor=*}`
- `holliverse.executor.queue.remaining{executor=*}`
- `holliverse.userlog.publish{event_name=*,result=*}`
- `holliverse.userlog.batch.size`
- `holliverse.userlog.admin_log_feature.duration{result=*}`

## MSK Lag

MSK lag는 애플리케이션이 직접 노출하는 메트릭이 아니라 AWS 측 메트릭입니다. 따라서 Grafana에서는 AWS CloudWatch datasource 또는 Prometheus로 수집한 exporter를 통해 별도 패널로 붙여야 합니다.

우선 봐야 할 메트릭:

- `SumOffsetLag`
- `MaxOffsetLag`
- `EstimatedTimeLag`
- `EstimatedMaxTimeLag`

권장 패널:

- consumer group별 `SumOffsetLag`
- consumer group별 `MaxOffsetLag`
- partition top N lag
- lag와 같은 화면에 `holliverse_executor_queue_size`, `holliverse_recommendation_pending_size`, DB pool, JVM, HTTP p95를 같이 배치

## 운영 기준

- 추천 API는 `timeout`, `pending.size`, `executor.queue.size{executor="recommendation-trigger"}`를 함께 봅니다.
- user-log는 `publish{result!="success"}`와 `executor.queue.size{executor="user-log"}`를 함께 봅니다.
- lag 기반 오토스케일링은 Grafana가 아니라 CloudWatch Alarm + ECS/EKS 오토스케일러가 수행합니다.
