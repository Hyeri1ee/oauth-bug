import requests
import datetime
from github import Github
from github import Auth
import os

private_access_token = os.environ['GITHUB_TOKEN']
DISCORD_URL = os.environ['DISCORD_WEBHOOK']
auth = Auth.Token(private_access_token)

repo_info = "Hyeri1ee/oauth-bug"

g = Github(auth=auth)
repo = g.get_repo(repo_info)

print(repo.full_name)

### pr이 만들어진 날짜 참고해서 D-N 줄이기
def _calculate_d_day(created_at: datetime.datetime) -> str:
    """
    PR 생성 시간을 기준으로 현재까지 경과된 날짜를 계산하여 D- 값을 반환
    """
    now = datetime.datetime.now(created_at.tzinfo)
    days_passed = (now - created_at).days

    if days_passed >= 5:
        return "D-0"
    else:
        return f"D-{5 - days_passed}"

### 라벨 컬러 설정
def _set_label_with_color(pull, label_name: str):
    """
    라벨을 설정하고 해당하는 색상을 지정합니다.
    D-0: 빨간색 (ff0000)
    D-1: 주황색 (ffa500)
    D-2: 노란색 (ffff00)
    D-3 이상: 파란색 (0000ff)
    """
    color_map = {
        "D-0": "ff0000",  # 빨간색
        "D-1": "ffa500",  # 주황색
        "D-2": "ffff00",  # 노란색
        "D-3": "0000ff",  # 파란색
        "D-4": "0000ff",  # 파란색
        "D-5": "0000ff",  # 파란색
    }

    # 기존 D- 라벨 삭제
    for label in pull.get_labels():
        if label.name.startswith("D-"):
            pull.remove_from_labels(label)

    # 새 라벨이 없으면 생성하고, 있으면 색상 업데이트
    try:
        label = repo.get_label(label_name)
        label.edit(name=label_name, color=color_map[label_name])
    except:
        repo.create_label(label_name, color_map[label_name])

    # 라벨 설정
    pull.add_to_labels(label_name)
    return label_name

### 라벨 태그
def set_pull_requests_tags():
    cnt, pulls = _get_total_pull_requests()

    pr_msg_to_discord = (
        f"@everyone 👋🏻 총 {cnt}개의 Pull Request가 소중한 리뷰를 기다리고 있어요! :smile:\n"
    )

    for pull in pulls:
        # PR 생성 시간 기준으로 D-day 계산
        d_day = _calculate_d_day(pull.created_at)

        # 라벨 설정
        _set_label_with_color(pull, d_day)

        pr_link = _make_pr_link_with_no(pull.number)
        pr_msg_to_discord += f"[{d_day}] [{pull.title}]({pr_link})\n"

    return pr_msg_to_discord

### 링크 설정
def _make_pr_link_with_no(pr_no: int) -> str:
    link = "https://github.com/" + str(repo.full_name) +"/pull/" + str(pr_no)
    return link

### opened 된 pr 가져오기
def _get_total_pull_requests():
    count = 0
    pull_requests_list = []

    #현재 열려있는 PR 목록들을 가져온다.
    for pull in repo.get_pulls(
            state="open",
            sort="updated",
    ):
        count += 1
        pull_requests_list.append(pull)

    return count,pull_requests_list

### 라벨 감소
# def _set_label_decrease(pull, before_label) -> str:
#     if before_label == "D-5":
#         return _set_label_with_color(pull, "D-4")
#     elif before_label == "D-4":
#         return _set_label_with_color(pull, "D-3")
#     elif before_label == "D-3":
#         return _set_label_with_color(pull, "D-2")
#     elif before_label == "D-2":
#         return _set_label_with_color(pull, "D-1")
#     else:
#         return _set_label_with_color(pull, "D-0")

### 최종 리턴되는 메시지 함수
def set_pull_requests_tags():
    cnt, pulls = _get_total_pull_requests()

    pr_msg_to_discord = (
        f"@everyone 👋🏻 총 {cnt}개의 Pull Request가 소중한 리뷰를 기다리고 있어요! :smile:\n"
    )

    for pull in pulls:
        labels = pull.get_labels()
        d_label = None

        # "D-" 로 시작하는 라벨 가져오기
        for label in labels:
            if label.name.startswith("D-"):
                d_label = label
                break

        pr_link = _make_pr_link_with_no(pull.number)

        if not d_label:
            # D- 라벨이 없다면 D-7로 설정
            pull.set_labels("D-7")
            pr_msg_to_discord += f"[[D-7] {pull.title}]({pr_link})\n"
        elif d_label.name == "D-0":
            # D-0 label case
            pr_msg_to_discord += f"[[D-0] {pull.title}]({pr_link})\n"
        else:
            # Decrease the D- label
            label = d_label.name

            pr_msg_to_discord += f"[[{label}] {pull.title}]({pr_link})\n"

    return pr_msg_to_discord

# 디스코드 채널로 메세지 전송
def discord_send_message(text):
    value = set_pull_requests_tags()
    print(value)
    now = datetime.datetime.now()
    message = {
        "content": "",  # 메인 메시지는 비워두고
        "embeds": [{
            "title": "Merge 안된 PR 목록",
            "description": str(text),
            "color": 5814783,  # 푸른색 계열
            "footer": {
                "text": f"알림 시각: {now.strftime('%Y-%m-%d %H:%M:%S')}"
            },
            "fields": [
                {
                    "name": "",
                    "value": value,
                    "inline": True
                }
            ]
        }]
    }

    response = requests.post(
        DISCORD_URL,
        json=message  # data 대신 json을 사용하면 자동으로 JSON 직렬화됨
    )

    if response.status_code == 204:  # Discord webhook은 성공시 204를 반환
        print("메시지 전송 성공")
    else:
        print(f"메시지 전송 실패: {response.status_code}")
    print(message)


discord_send_message("KU:Room 백엔드 레포")
