let h2 = document.getElementsByTagName('h2')[0];
let old_date_string = h2.firstChild.textContent.split(': ')[1];
let old_date = new Date(old_date_string);

let now_date = new Date();


if(old_date.setHours(0,0,0,0) !== now_date.setHours(0,0,0,0)) {
    h2.classList.add("old");
} else {
    h2.classList.remove("old");
}
