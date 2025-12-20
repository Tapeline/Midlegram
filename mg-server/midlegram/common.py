from dataclasses import dataclass
from typing import dataclass_transform


@dataclass_transform(frozen_default=True)
def dto[DTO_T](cls: type[DTO_T]) -> type[DTO_T]:
    return dataclass(slots=True, frozen=True)(cls)


@dataclass_transform(frozen_default=True)
def entity[Entity_T](cls: type[Entity_T]) -> type[Entity_T]:
    return dataclass(slots=True, frozen=True)(cls)


@dataclass_transform(frozen_default=True)
def interactor[Interactor_T](cls: type[Interactor_T]) -> type[Interactor_T]:
    return dataclass(slots=True, frozen=True)(cls)
