import math
from random import randint, uniform


def generate_decimal_number(no_of_digits: int):
    min_value = int(math.pow(10, no_of_digits - 1))
    max_value = int(math.pow(10, no_of_digits) - 1)
    return randint(min_value, max_value)


def generate_int_in_range(min_value, max_value):
    return randint(min_value, max_value)


def generate_random_lon():
    min_lon = 13.0
    max_lon = 18.0
    return uniform(min_lon, max_lon)


def generate_random_lat():
    min_lat = 56.0
    max_lat = 64.0
    return uniform(min_lat, max_lat)
