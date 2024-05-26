

function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i].trim();
        if (cookie.startsWith(name + '=')) {
            return cookie.substring(name.length + 1);
        }
    }
    return null;
}

async function getUserInfo(accessToken) {
	return new Promise(function(resolve, reject) {
        var url = "/api/user";
        $.ajax({
            url: url,
            type: "get",
            contentType: 'application/json',
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + accessToken);
            },
            success: function(data) {
                console.log(data);
                resolve(data); // 성공 시 프라미스를 이행하고 데이터 반환
            },
            error: function(request, status, error) {
                reject(error); // 실패 시 프라미스를 거부하고 에러 반환
            }
        });
    });
}


function getTaskStatusName(code) {
	if(code == 0) {
		return "할 일";
	} else if(code == 1) {
		return "진행 중";
	} else if(code == 2) {
		return "완료";
	} else if(code == 3) {
		return "대기";
	}
}

function getTaskStatusColorClass(code) {
	if(code == 0) {
		return "";
	} else if(code == 1) {
		return "status-in-progress";
	} else if(code == 2) {
		return "status-completed";
	} else if(code == 3) {
		return "status-pending";
	}
}

function getWeightName(code) {
	if(code == 20) {
		return "낮음";
	} else if(code == 30) {
		return "보통";
	} else if(code == 50) {
		return "높음";
	} else if(code == 80) {
		return "매우 높음";
	}
}

function relativeTimeFromNow(dateString) {
	moment.locale('ko');
  	return moment(dateString).fromNow();
}

function validatePassword(password) {
    // 비밀번호의 길이가 8자 이상인지 확인
    if (password.length < 8) {
        return false;
    }

    // 각각의 문자열 유형에 대한 정규식 패턴
    var patterns = [
        /[a-z]/, // 소문자
        /[A-Z]/, // 대문자
        /\d/,    // 숫자
        /[!@#$%^&*(),.?":{}|<>]/ // 특수문자
    ];

    // 모든 패턴이 비밀번호에 적어도 하나씩 존재하는지 확인
    for (var i = 0; i < patterns.length; i++) {
        if (!patterns[i].test(password)) {
            return false;
        }
    }

    // 모든 조건을 만족하면 유효한 비밀번호로 간주
    return true;
}
